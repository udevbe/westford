package org.westmalle.wayland.nativ.libpng;

import org.freedesktop.jaccall.Lib;
import org.freedesktop.jaccall.Pointer;
import org.freedesktop.jaccall.Ptr;
import org.freedesktop.jaccall.Unsigned;

@Lib("png")
public class Libpng {
    @Ptr
    public native long png_create_write_struct(@Ptr(char.class) long user_png_ver,
                                               @Ptr(Void.class) long error_ptr,
                                               @Ptr(png_error_ptr.class) long error_fn,
                                               @Ptr(png_error_ptr.class) long warn_fn);

    @Ptr
    public native long png_create_info_struct(@Ptr long png_ptr);

    public native void png_set_IHDR(@Ptr long png_ptr,
                                    @Ptr long info_ptr,
                                    @Unsigned int width,
                                    @Unsigned int height,
                                    int bit_depth,
                                    int color_type,
                                    int interlace_type,
                                    int compression_type,
                                    int filter_type);

    public native void png_set_rows(@Ptr long png_ptr,
                                    @Ptr long info_ptr,
                                    @Ptr(Pointer.class)/*byte*/ long row_pointers);

    public native void png_set_write_fn(@Ptr long png_ptr,
                                        @Ptr(Void.class) long io_ptr,
                                        @Ptr(png_rw_ptr.class) long write_data_fn,
                                        @Ptr(png_flush_ptr.class) long output_flush_fn);

    public native void png_write_png(@Ptr long png_ptr,
                                     @Ptr long info_ptr,
                                     int transforms,
                                     @Ptr(Void.class) long params);

    @Ptr
    public native long png_get_io_ptr(@Ptr long png_ptr);

    @Ptr
    public native long png_jmpbuf(@Ptr long png_ptr);
}
