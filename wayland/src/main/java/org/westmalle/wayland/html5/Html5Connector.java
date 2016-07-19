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
import static org.westmalle.wayland.nativ.libpng.Pointerpng_rw_ptr.nref;

@AutoFactory(allowSubclasses = true,
             className = "Html5ConnectorFactory")
public class Html5Connector implements Connector {


    private static final byte[] red_border = {
            (byte) 0x89, (byte) 0x50, (byte) 0x4e, (byte) 0x47, (byte) 0x0d, (byte) 0x0a, (byte) 0x1a, (byte) 0x0a, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x0d,
            0x49, (byte) 0x48, (byte) 0x44, (byte) 0x52, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x0e, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x0e,
            0x08, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x28, (byte) 0x96, (byte) 0xdd, (byte) 0xe3, (byte) 0x00, (byte) 0x00, (byte) 0x02,
            (byte) 0x88, (byte) 0x50, (byte) 0x4c, (byte) 0x54, (byte) 0x45, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xcc, (byte) 0xff,
            (byte) 0xff, (byte) 0x99, (byte) 0xff, (byte) 0xff, (byte) 0x66, (byte) 0xff, (byte) 0xff, (byte) 0x33, (byte) 0xff, (byte) 0xff, (byte) 0x00, (byte) 0xff,
            (byte) 0xcc, (byte) 0xff, (byte) 0xff, (byte) 0xcc, (byte) 0xcc, (byte) 0xff, (byte) 0xcc, (byte) 0x99, (byte) 0xff, (byte) 0xcc, (byte) 0x66, (byte) 0xff,
            (byte) 0xcc, (byte) 0x33, (byte) 0xff, (byte) 0xcc, (byte) 0x00, (byte) 0xff, (byte) 0x99, (byte) 0xff, (byte) 0xff, (byte) 0x99, (byte) 0xcc, (byte) 0xff,
            (byte) 0x99, (byte) 0x99, (byte) 0xff, (byte) 0x99, (byte) 0x66, (byte) 0xff, (byte) 0x99, (byte) 0x33, (byte) 0xff, (byte) 0x99, (byte) 0x00, (byte) 0xff,
            0x66, (byte) 0xff, (byte) 0xff, (byte) 0x66, (byte) 0xcc, (byte) 0xff, (byte) 0x66, (byte) 0x99, (byte) 0xff, (byte) 0x66, (byte) 0x66, (byte) 0xff,
            0x66, (byte) 0x33, (byte) 0xff, (byte) 0x66, (byte) 0x00, (byte) 0xff, (byte) 0x33, (byte) 0xff, (byte) 0xff, (byte) 0x33, (byte) 0xcc, (byte) 0xff,
            0x33, (byte) 0x99, (byte) 0xff, (byte) 0x33, (byte) 0x66, (byte) 0xff, (byte) 0x33, (byte) 0x33, (byte) 0xff, (byte) 0x33, (byte) 0x00, (byte) 0xff,
            0x00, (byte) 0xff, (byte) 0xff, (byte) 0x00, (byte) 0xcc, (byte) 0xff, (byte) 0x00, (byte) 0x99, (byte) 0xff, (byte) 0x00, (byte) 0x66, (byte) 0xff,
            0x00, (byte) 0x33, (byte) 0xff, (byte) 0x00, (byte) 0x00, (byte) 0xcc, (byte) 0xff, (byte) 0xff, (byte) 0xcc, (byte) 0xff, (byte) 0xcc, (byte) 0xcc,
            (byte) 0xff, (byte) 0x99, (byte) 0xcc, (byte) 0xff, (byte) 0x66, (byte) 0xcc, (byte) 0xff, (byte) 0x33, (byte) 0xcc, (byte) 0xff, (byte) 0x00, (byte) 0xcc,
            (byte) 0xcc, (byte) 0xff, (byte) 0xcc, (byte) 0xcc, (byte) 0xcc, (byte) 0xcc, (byte) 0xcc, (byte) 0x99, (byte) 0xcc, (byte) 0xcc, (byte) 0x66, (byte) 0xcc,
            (byte) 0xcc, (byte) 0x33, (byte) 0xcc, (byte) 0xcc, (byte) 0x00, (byte) 0xcc, (byte) 0x99, (byte) 0xff, (byte) 0xcc, (byte) 0x99, (byte) 0xcc, (byte) 0xcc,
            (byte) 0x99, (byte) 0x99, (byte) 0xcc, (byte) 0x99, (byte) 0x66, (byte) 0xcc, (byte) 0x99, (byte) 0x33, (byte) 0xcc, (byte) 0x99, (byte) 0x00, (byte) 0xcc,
            0x66, (byte) 0xff, (byte) 0xcc, (byte) 0x66, (byte) 0xcc, (byte) 0xcc, (byte) 0x66, (byte) 0x99, (byte) 0xcc, (byte) 0x66, (byte) 0x66, (byte) 0xcc,
            0x66, (byte) 0x33, (byte) 0xcc, (byte) 0x66, (byte) 0x00, (byte) 0xcc, (byte) 0x33, (byte) 0xff, (byte) 0xcc, (byte) 0x33, (byte) 0xcc, (byte) 0xcc,
            0x33, (byte) 0x99, (byte) 0xcc, (byte) 0x33, (byte) 0x66, (byte) 0xcc, (byte) 0x33, (byte) 0x33, (byte) 0xcc, (byte) 0x33, (byte) 0x00, (byte) 0xcc,
            0x00, (byte) 0xff, (byte) 0xcc, (byte) 0x00, (byte) 0xcc, (byte) 0xcc, (byte) 0x00, (byte) 0x99, (byte) 0xcc, (byte) 0x00, (byte) 0x66, (byte) 0xcc,
            0x00, (byte) 0x33, (byte) 0xcc, (byte) 0x00, (byte) 0x00, (byte) 0x99, (byte) 0xff, (byte) 0xff, (byte) 0x99, (byte) 0xff, (byte) 0xcc, (byte) 0x99,
            (byte) 0xff, (byte) 0x99, (byte) 0x99, (byte) 0xff, (byte) 0x66, (byte) 0x99, (byte) 0xff, (byte) 0x33, (byte) 0x99, (byte) 0xff, (byte) 0x00, (byte) 0x99,
            (byte) 0xcc, (byte) 0xff, (byte) 0x99, (byte) 0xcc, (byte) 0xcc, (byte) 0x99, (byte) 0xcc, (byte) 0x99, (byte) 0x99, (byte) 0xcc, (byte) 0x66, (byte) 0x99,
            (byte) 0xcc, (byte) 0x33, (byte) 0x99, (byte) 0xcc, (byte) 0x00, (byte) 0x99, (byte) 0x99, (byte) 0xff, (byte) 0x99, (byte) 0x99, (byte) 0xcc, (byte) 0x99,
            (byte) 0x99, (byte) 0x99, (byte) 0x99, (byte) 0x99, (byte) 0x66, (byte) 0x99, (byte) 0x99, (byte) 0x33, (byte) 0x99, (byte) 0x99, (byte) 0x00, (byte) 0x99,
            0x66, (byte) 0xff, (byte) 0x99, (byte) 0x66, (byte) 0xcc, (byte) 0x99, (byte) 0x66, (byte) 0x99, (byte) 0x99, (byte) 0x66, (byte) 0x66, (byte) 0x99,
            0x66, (byte) 0x33, (byte) 0x99, (byte) 0x66, (byte) 0x00, (byte) 0x99, (byte) 0x33, (byte) 0xff, (byte) 0x99, (byte) 0x33, (byte) 0xcc, (byte) 0x99,
            0x33, (byte) 0x99, (byte) 0x99, (byte) 0x33, (byte) 0x66, (byte) 0x99, (byte) 0x33, (byte) 0x33, (byte) 0x99, (byte) 0x33, (byte) 0x00, (byte) 0x99,
            0x00, (byte) 0xff, (byte) 0x99, (byte) 0x00, (byte) 0xcc, (byte) 0x99, (byte) 0x00, (byte) 0x99, (byte) 0x99, (byte) 0x00, (byte) 0x66, (byte) 0x99,
            0x00, (byte) 0x33, (byte) 0x99, (byte) 0x00, (byte) 0x00, (byte) 0x66, (byte) 0xff, (byte) 0xff, (byte) 0x66, (byte) 0xff, (byte) 0xcc, (byte) 0x66,
            (byte) 0xff, (byte) 0x99, (byte) 0x66, (byte) 0xff, (byte) 0x66, (byte) 0x66, (byte) 0xff, (byte) 0x33, (byte) 0x66, (byte) 0xff, (byte) 0x00, (byte) 0x66,
            (byte) 0xcc, (byte) 0xff, (byte) 0x66, (byte) 0xcc, (byte) 0xcc, (byte) 0x66, (byte) 0xcc, (byte) 0x99, (byte) 0x66, (byte) 0xcc, (byte) 0x66, (byte) 0x66,
            (byte) 0xcc, (byte) 0x33, (byte) 0x66, (byte) 0xcc, (byte) 0x00, (byte) 0x66, (byte) 0x99, (byte) 0xff, (byte) 0x66, (byte) 0x99, (byte) 0xcc, (byte) 0x66,
            (byte) 0x99, (byte) 0x99, (byte) 0x66, (byte) 0x99, (byte) 0x66, (byte) 0x66, (byte) 0x99, (byte) 0x33, (byte) 0x66, (byte) 0x99, (byte) 0x00, (byte) 0x66,
            0x66, (byte) 0xff, (byte) 0x66, (byte) 0x66, (byte) 0xcc, (byte) 0x66, (byte) 0x66, (byte) 0x99, (byte) 0x66, (byte) 0x66, (byte) 0x66, (byte) 0x66,
            0x66, (byte) 0x33, (byte) 0x66, (byte) 0x66, (byte) 0x00, (byte) 0x66, (byte) 0x33, (byte) 0xff, (byte) 0x66, (byte) 0x33, (byte) 0xcc, (byte) 0x66,
            0x33, (byte) 0x99, (byte) 0x66, (byte) 0x33, (byte) 0x66, (byte) 0x66, (byte) 0x33, (byte) 0x33, (byte) 0x66, (byte) 0x33, (byte) 0x00, (byte) 0x66,
            0x00, (byte) 0xff, (byte) 0x66, (byte) 0x00, (byte) 0xcc, (byte) 0x66, (byte) 0x00, (byte) 0x99, (byte) 0x66, (byte) 0x00, (byte) 0x66, (byte) 0x66,
            0x00, (byte) 0x33, (byte) 0x66, (byte) 0x00, (byte) 0x00, (byte) 0x33, (byte) 0xff, (byte) 0xff, (byte) 0x33, (byte) 0xff, (byte) 0xcc, (byte) 0x33,
            (byte) 0xff, (byte) 0x99, (byte) 0x33, (byte) 0xff, (byte) 0x66, (byte) 0x33, (byte) 0xff, (byte) 0x33, (byte) 0x33, (byte) 0xff, (byte) 0x00, (byte) 0x33,
            (byte) 0xcc, (byte) 0xff, (byte) 0x33, (byte) 0xcc, (byte) 0xcc, (byte) 0x33, (byte) 0xcc, (byte) 0x99, (byte) 0x33, (byte) 0xcc, (byte) 0x66, (byte) 0x33,
            (byte) 0xcc, (byte) 0x33, (byte) 0x33, (byte) 0xcc, (byte) 0x00, (byte) 0x33, (byte) 0x99, (byte) 0xff, (byte) 0x33, (byte) 0x99, (byte) 0xcc, (byte) 0x33,
            (byte) 0x99, (byte) 0x99, (byte) 0x33, (byte) 0x99, (byte) 0x66, (byte) 0x33, (byte) 0x99, (byte) 0x33, (byte) 0x33, (byte) 0x99, (byte) 0x00, (byte) 0x33,
            0x66, (byte) 0xff, (byte) 0x33, (byte) 0x66, (byte) 0xcc, (byte) 0x33, (byte) 0x66, (byte) 0x99, (byte) 0x33, (byte) 0x66, (byte) 0x66, (byte) 0x33,
            0x66, (byte) 0x33, (byte) 0x33, (byte) 0x66, (byte) 0x00, (byte) 0x33, (byte) 0x33, (byte) 0xff, (byte) 0x33, (byte) 0x33, (byte) 0xcc, (byte) 0x33,
            0x33, (byte) 0x99, (byte) 0x33, (byte) 0x33, (byte) 0x66, (byte) 0x33, (byte) 0x33, (byte) 0x33, (byte) 0x33, (byte) 0x33, (byte) 0x00, (byte) 0x33,
            0x00, (byte) 0xff, (byte) 0x33, (byte) 0x00, (byte) 0xcc, (byte) 0x33, (byte) 0x00, (byte) 0x99, (byte) 0x33, (byte) 0x00, (byte) 0x66, (byte) 0x33,
            0x00, (byte) 0x33, (byte) 0x33, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xff, (byte) 0xff, (byte) 0x00, (byte) 0xff, (byte) 0xcc, (byte) 0x00,
            (byte) 0xff, (byte) 0x99, (byte) 0x00, (byte) 0xff, (byte) 0x66, (byte) 0x00, (byte) 0xff, (byte) 0x33, (byte) 0x00, (byte) 0xff, (byte) 0x00, (byte) 0x00,
            (byte) 0xcc, (byte) 0xff, (byte) 0x00, (byte) 0xcc, (byte) 0xcc, (byte) 0x00, (byte) 0xcc, (byte) 0x99, (byte) 0x00, (byte) 0xcc, (byte) 0x66, (byte) 0x00,
            (byte) 0xcc, (byte) 0x33, (byte) 0x00, (byte) 0xcc, (byte) 0x00, (byte) 0x00, (byte) 0x99, (byte) 0xff, (byte) 0x00, (byte) 0x99, (byte) 0xcc, (byte) 0x00,
            (byte) 0x99, (byte) 0x99, (byte) 0x00, (byte) 0x99, (byte) 0x66, (byte) 0x00, (byte) 0x99, (byte) 0x33, (byte) 0x00, (byte) 0x99, (byte) 0x00, (byte) 0x00,
            0x66, (byte) 0xff, (byte) 0x00, (byte) 0x66, (byte) 0xcc, (byte) 0x00, (byte) 0x66, (byte) 0x99, (byte) 0x00, (byte) 0x66, (byte) 0x66, (byte) 0x00,
            0x66, (byte) 0x33, (byte) 0x00, (byte) 0x66, (byte) 0x00, (byte) 0x00, (byte) 0x33, (byte) 0xff, (byte) 0x00, (byte) 0x33, (byte) 0xcc, (byte) 0x00,
            0x33, (byte) 0x99, (byte) 0x00, (byte) 0x33, (byte) 0x66, (byte) 0x00, (byte) 0x33, (byte) 0x33, (byte) 0x00, (byte) 0x33, (byte) 0x00, (byte) 0x00,
            0x00, (byte) 0xff, (byte) 0x00, (byte) 0x00, (byte) 0xcc, (byte) 0x00, (byte) 0x00, (byte) 0x99, (byte) 0x00, (byte) 0x00, (byte) 0x66, (byte) 0x00,
            0x00, (byte) 0x33, (byte) 0xee, (byte) 0x00, (byte) 0x00, (byte) 0xa8, (byte) 0xf6, (byte) 0xef, (byte) 0x70, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            0x21, (byte) 0x74, (byte) 0x45, (byte) 0x58, (byte) 0x74, (byte) 0x53, (byte) 0x6f, (byte) 0x66, (byte) 0x74, (byte) 0x77, (byte) 0x61, (byte) 0x72,
            0x65, (byte) 0x00, (byte) 0x47, (byte) 0x72, (byte) 0x61, (byte) 0x70, (byte) 0x68, (byte) 0x69, (byte) 0x63, (byte) 0x43, (byte) 0x6f, (byte) 0x6e,
            0x76, (byte) 0x65, (byte) 0x72, (byte) 0x74, (byte) 0x65, (byte) 0x72, (byte) 0x20, (byte) 0x28, (byte) 0x49, (byte) 0x6e, (byte) 0x74, (byte) 0x65,
            0x6c, (byte) 0x29, (byte) 0x77, (byte) 0x87, (byte) 0xfa, (byte) 0x19, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x21, (byte) 0x49, (byte) 0x44,
            0x41, (byte) 0x54, (byte) 0x78, (byte) 0x9c, (byte) 0x62, (byte) 0x60, (byte) 0x60, (byte) 0xb8, (byte) 0x0e, (byte) 0x07, (byte) 0x0c, (byte) 0x20,
            (byte) 0x80, (byte) 0xc1, (byte) 0x65, (byte) 0x40, (byte) 0xa6, (byte) 0xa9, (byte) 0xca, (byte) 0xc5, (byte) 0x67, (byte) 0x2f, (byte) 0xd9, (byte) 0x00,
            0x00, (byte) 0x00, (byte) 0x00, (byte) 0xff, (byte) 0xff, (byte) 0x03, (byte) 0x00, (byte) 0x5a, (byte) 0xb6, (byte) 0x35, (byte) 0xc1, (byte) 0xba,
            (byte) 0xd4, (byte) 0xa9, (byte) 0xf8, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x49, (byte) 0x45, (byte) 0x4e, (byte) 0x44, (byte) 0xae,
            (byte) 0x42, (byte) 0x60, (byte) 0x82
    };

    @Nonnull
    private final Libc      libc;
    @Nonnull
    private final Libpng    libpng;
    private final Connector connector;

    private final Set<Html5Socket> html5Sockets = new HashSet<>();

    private final Pointer<png_rw_ptr> pngWriteCallback = nref(this::pngWriteCallback);
    private ByteBuffer targetPngFrame;


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

        //TODO we could reuse the targetPngFrame and only allocate a new one if the resolution changed
        this.targetPngFrame = ByteBuffer.allocate(maxPNGSize(pitch,
                                                             height));
        //this.targetPngFrame.put(red_border);
        toPng(bufferRGBA,
              pitch,
              height);

        this.targetPngFrame.rewind();
        getHtml5Sockets()
                .forEach(html5Socket ->
                                 html5Socket.getSession()
                                            .ifPresent(session -> {
                                                //TODO we should send frame were unchanged pixels have an rgba int of 0,
                                                //this will make the png compression much better. However this means we need
                                                //to resend a full frame when a delta frame was not received and as such need
                                                //to keep track of frame delivery per remote.

                                                //we send full frames for now so we don't care about delivery state.
                                                try {
                                                    session.getRemote()
                                                           .sendBytes(this.targetPngFrame);
                                                }
                                                catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                            }));
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
        for (int y = 0; y < height; ++y) {
            rows.writei(y,
                        sourceRGBA.offset(y * pitch * 4));
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
