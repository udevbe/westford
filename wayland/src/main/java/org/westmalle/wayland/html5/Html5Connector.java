package org.westmalle.wayland.html5;


import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import org.eclipse.jetty.websocket.api.BatchMode;
import org.eclipse.jetty.websocket.api.Session;
import org.freedesktop.jaccall.JNI;
import org.freedesktop.jaccall.Lng;
import org.freedesktop.jaccall.Pointer;
import org.freedesktop.jaccall.Ptr;
import org.freedesktop.jaccall.Unsigned;
import org.westmalle.wayland.core.Connector;
import org.westmalle.wayland.core.Renderer;
import org.westmalle.wayland.nativ.libpng.Libpng;
import org.westmalle.wayland.nativ.libpng.png_rw_ptr;
import org.westmalle.wayland.protocol.WlOutput;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.freedesktop.jaccall.Pointer.malloc;
import static org.freedesktop.jaccall.Size.sizeof;
import static org.westmalle.wayland.nativ.libpng.Libpng.PNG_COLOR_TYPE_RGBA;
import static org.westmalle.wayland.nativ.libpng.Libpng.PNG_COMPRESSION_TYPE_DEFAULT;
import static org.westmalle.wayland.nativ.libpng.Libpng.PNG_FILTER_TYPE_DEFAULT;
import static org.westmalle.wayland.nativ.libpng.Libpng.PNG_INTERLACE_NONE;
import static org.westmalle.wayland.nativ.libpng.Libpng.PNG_TRANSFORM_IDENTITY;
import static org.westmalle.wayland.nativ.libpng.Pointerpng_rw_ptr.nref;

@AutoFactory(allowSubclasses = true,
             className = "Html5ConnectorFactory")
public class Html5Connector implements Connector {

    @Nonnull
    private final Libpng    libpng;
    private final Connector connector;

    private final Set<Html5Socket> html5Sockets = new HashSet<>();

    private final Pointer<png_rw_ptr> pngWriteCallback = nref(this::pngWriteCallback);
    private ByteBuffer targetPngFrame;

    //FIXME busy flag granularity should be on a per websocket connection level
    private final AtomicBoolean   commitBusy = new AtomicBoolean(false);
    private       ExecutorService sender     = Executors.newSingleThreadExecutor();

    Html5Connector(@Provided @Nonnull final Libpng libpng,
                   @Nonnull final Connector connector) {
        this.libpng = libpng;
        this.connector = connector;
    }

    @Nonnull
    @Override
    public WlOutput getWlOutput() {
        return this.connector.getWlOutput();
    }

    //TODO add methods to send screen updates to all connected sockets

    /**
     * @param bufferRGBA
     * @param flipHorizontal
     * @param pitch
     * @param height
     *
     * @return true if bufferRGBA will be consumed & freed.
     */
    public boolean commitFrame(final Pointer<Byte> bufferRGBA,
                               final boolean flipHorizontal,
                               final int pitch,
                               final int height) {
        if (this.commitBusy.compareAndSet(false,
                                          true)) {
            this.sender.submit(() -> {
                //TODO we could reuse the targetPngFrame and only allocate a new one if the resolution changed
                this.targetPngFrame = ByteBuffer.allocate(maxPNGSize(pitch,
                                                                     height));
                //this.targetPngFrame.put(red_border);
                toPng(bufferRGBA,
                      flipHorizontal,
                      pitch,
                      height);

                this.targetPngFrame.rewind();
                this.html5Sockets.forEach(html5Socket ->
                                                  html5Socket.getSession()
                                                             .ifPresent(session -> {
                                                                 //TODO we should send frame where unchanged pixels have an rgba int of 0,
                                                                 //this will make the png compression much better. However this means we need
                                                                 //to resend a full frame when a delta frame was not received and as such need
                                                                 //to keep track of frame delivery per remote.

                                                                 //we send full frames for now so we don't care about delivery state.
                                                                 session.getRemote()
                                                                        .sendBytesByFuture(this.targetPngFrame);

                                                             }));
                bufferRGBA.close();
            });

            return true;
        }
        else {
            return false;
        }
    }

    public boolean getCommitBusy() {
        return this.commitBusy.get();
    }

    private int maxPNGSize(final int width,
                           final int height) {
        return 8 // PNG signature bytes
               + 25 // IHDR chunk
               + 12 // IDAT chunk (assuming only one IDAT chunk)
               + height //pixels
                 * (1 // filter byte for each row
                    + (width // pixels
                       * 3 // Red, blue, green color samples
                       * 2 // 16 bits per color sample
                    )
                 )
               + 6 // zlib compression overhead
               + 2 // deflate overhead
               + 12; // IEND chunk
    }

    private void toPng(final Pointer<Byte> sourceRGBA,
                       final boolean flipHorizontal,
                       final int pitch,
                       final int height) {

        final long p = this.libpng.png_create_write_struct(nref("1.6.23+apng").address,
                                                           0L,
                                                           0L,
                                                           0L);
        if (p == 0L) {
            throw new RuntimeException("png_create_write_struct() failed");
        }

        final long infoPtr = this.libpng.png_create_info_struct(p);
        if (infoPtr == 0L) {
            throw new RuntimeException("png_create_info_struct() failed");
        }

//        if (0 != this.libc.setjmp(this.libpng.png_jmpbuf(p))) {
//            throw new RuntimeException("setjmp(png_jmpbuf(p) failed");
//        }

        this.libpng.png_set_IHDR(p,
                                 infoPtr,
                                 pitch,
                                 height,
                                 8,
                                 PNG_COLOR_TYPE_RGBA,
                                 PNG_INTERLACE_NONE,
                                 PNG_COMPRESSION_TYPE_DEFAULT,
                                 PNG_FILTER_TYPE_DEFAULT);

        final Pointer<Pointer<Byte>> rows = malloc(height * sizeof((Pointer) null),
                                                   Byte.class).castpp();
        if (flipHorizontal) {
            int row = 0;
            for (int y = height - 1; y >= 0; --y, row++) {
                rows.writei(row,
                            sourceRGBA.offset(y * pitch * 4));
            }
        }
        else {
            for (int y = 0; y < height; ++y) {
                rows.writei(y,
                            sourceRGBA.offset(y * pitch * 4));
            }
        }
        this.libpng.png_set_rows(p,
                                 infoPtr,
                                 rows.address);

        this.libpng.png_set_write_fn(p,
                                     0L,
                                     this.pngWriteCallback.address,
                                     0L);
        this.libpng.png_write_png(p,
                                  infoPtr,
                                  PNG_TRANSFORM_IDENTITY,
                                  0L);

        rows.close();
        this.libpng.png_destroy_write_struct(Pointer.nref(Pointer.wrap(p)).address,
                                             Pointer.nref(Pointer.wrap(infoPtr)).address);
    }

    private void pngWriteCallback(@Ptr final long png_ptr,
                                  @Ptr(byte.class) final long png_bytep,
                                  @Unsigned @Lng final long png_size_t) {
        for (int i = 0; i < png_size_t; i++) {
            this.targetPngFrame.put(JNI.readByte(png_bytep,
                                                 i));
        }
    }


    //TODO handle socket input events

    public void onWebSocketBinary(final Html5Socket html5Socket,
                                  final byte[] payload,
                                  final int offset,
                                  final int len) {
        //no-op
    }

    public void onWebSocketText(final Html5Socket html5Socket,
                                final String message) {

        switch (message) {
            case "req-output-info": {
                handleOutputInfoRequest(html5Socket);
                break;
            }
            case "ack-output-info": {
                //enable sending frames
                this.html5Sockets.add(html5Socket);
                break;
            }
            case "ack-frame": {
                //wait until browser confirms it has rendered the frame we send.
                this.commitBusy.set(false);
            }
            default: {
                //TODO move to html5 seat implementation
                handleInput(message);
            }
        }
    }

    private void handleInput(final String message) {
        switch (message.substring(0,
                                  4)) {
            case "p:d:": {

            }
            case "p:u:": {

            }
            case "p:m:": {

            }
            case "k:d:": {

            }
            case "k:u:": {

            }
        }
    }

    private void handleOutputInfoRequest(final Html5Socket html5Socket) {
        html5Socket.getSession()
                   .ifPresent(session -> {
                       try {
                           //TODO send output info
                           session.getRemote()
                                  .sendString("output info here");
                       }
                       catch (final IOException e) {
                           //TODO log
                           e.printStackTrace();
                       }
                   });
    }

    public void onWebSocketClose(final Html5Socket html5Socket,
                                 final int statusCode,
                                 final String reason) {
        this.html5Sockets.remove(html5Socket);
    }

    public void onWebSocketConnect(final Html5Socket html5Socket,
                                   final Session session) {
        //no op
    }

    public void onWebSocketError(final Html5Socket html5Socket,
                                 final Throwable cause) {
        //TODO log
    }

    @Override
    public void accept(@Nonnull final Renderer renderer) {
        renderer.visit(this.connector);
    }
}