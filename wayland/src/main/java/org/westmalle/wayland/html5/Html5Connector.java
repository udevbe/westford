package org.westmalle.wayland.html5;


import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import org.eclipse.jetty.websocket.api.Session;
import org.freedesktop.jaccall.JNI;
import org.freedesktop.jaccall.Lng;
import org.freedesktop.jaccall.Pointer;
import org.freedesktop.jaccall.Ptr;
import org.freedesktop.jaccall.Unsigned;
import org.westmalle.wayland.core.Connector;
import org.westmalle.wayland.core.Renderer;
import org.westmalle.wayland.nativ.libc.Libc;
import org.westmalle.wayland.nativ.libpng.Libpng;
import org.westmalle.wayland.nativ.libpng.Pointerpng_rw_ptr;
import org.westmalle.wayland.nativ.libpng.png_rw_ptr;
import org.westmalle.wayland.protocol.WlOutput;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

import static org.freedesktop.jaccall.Pointer.malloc;
import static org.freedesktop.jaccall.Size.sizeof;
import static org.westmalle.wayland.nativ.libpng.Libpng.PNG_COLOR_TYPE_RGBA;
import static org.westmalle.wayland.nativ.libpng.Libpng.PNG_COMPRESSION_TYPE_DEFAULT;
import static org.westmalle.wayland.nativ.libpng.Libpng.PNG_FILTER_TYPE_DEFAULT;
import static org.westmalle.wayland.nativ.libpng.Libpng.PNG_INTERLACE_NONE;
import static org.westmalle.wayland.nativ.libpng.Libpng.PNG_TRANSFORM_IDENTITY;

@AutoFactory(allowSubclasses = true,
             className = "Html5ConnectorFactory")
public class Html5Connector implements Connector {

    @Nonnull
    private final Libc      libc;
    @Nonnull
    private final Libpng    libpng;
    private final Connector connector;

    private final Set<Html5Socket> html5Sockets = new HashSet<>();

    private final Pointer<png_rw_ptr> pngWriteCallback = Pointerpng_rw_ptr.nref(this::pngWriteCallback);

    Html5Connector(@Provided @Nonnull final Libc libc,
                   @Provided @Nonnull final Libpng libpng,
                   @Nonnull final Connector connector) {
        this.libc = libc;
        this.libpng = libpng;
        this.connector = connector;
    }

    @Nonnull
    @Override
    public WlOutput getWlOutput() {
        return this.connector.getWlOutput();
    }

    public Set<Html5Socket> getHtml5Sockets() {
        return this.html5Sockets;
    }

    //TODO add methods to send screen updates to all connected sockets
    public void commitFrame(final Pointer<Byte> bufferRGBA,
                            final int pitch,
                            final int height) {
        final Pointer<Pointer<Byte>> targetPNG = malloc(sizeof((Pointer) null),
                                                        Byte.class).castpp();
        toPng(bufferRGBA,
              targetPNG,
              pitch,
              height);
        final ByteBuffer pngByteBuffer = JNI.wrap(targetPNG.address,
                                                  pitch * height * 4);
        getHtml5Sockets()
                .forEach(html5Socket ->
                                 html5Socket.getSession()
                                            .ifPresent(session -> {
                                                //TODO we should send frame were unchanged pixels have an rgba int of 0,
                                                //this will make the png compression much better. However this means we need
                                                //to resend a full frame when a delta frame was not received and as such need
                                                //to keep track of frame delivery per remote.

                                                //we send full frames for now so we don't care about delivery state.
                                                session.getRemote()
                                                       .sendBytesByFuture(pngByteBuffer);
                                            }));
        targetPNG.close();
    }

    private void toPng(final Pointer<Byte> sourceRGBA,
                       final Pointer<Pointer<Byte>> targetPNG,
                       final int pitch,
                       final int height) {
        final long p = this.libpng.png_create_write_struct(Pointer.nref("1.6.23+apng").address,
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

        if (0 != this.libc.setjmp(this.libpng.png_jmpbuf(p))) {
            throw new RuntimeException("setjmp(png_jmpbuf(p) failed");
        }

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
        for (int y = 0; y < height; ++y) {
            rows.writei(y,
                        sourceRGBA.offset(y * pitch * 4));
        }
        this.libpng.png_set_rows(p,
                                 infoPtr,
                                 rows.address);
        rows.close();

        this.libpng.png_set_write_fn(p,
                                     targetPNG.address,
                                     this.pngWriteCallback.address,
                                     0L);
        this.libpng.png_write_png(p,
                                  infoPtr,
                                  PNG_TRANSFORM_IDENTITY,
                                  0L);
    }

    private void pngWriteCallback(@Ptr final long png_ptr,
                                  @Ptr(byte.class) final long png_bytep,
                                  @Unsigned @Lng final long png_size_t) {
        final Pointer<Pointer<Byte>> ioPtr = Pointer.wrap(Byte.class,
                                                          this.libpng.png_get_io_ptr(png_ptr))
                                                    .castpp();
        final Pointer<Byte> pngBuffer = malloc((int) png_size_t,
                                               Byte.class);
        this.libc.memcpy(pngBuffer.address,
                         png_bytep,
                         (int) png_size_t);
        ioPtr.write(pngBuffer);
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
