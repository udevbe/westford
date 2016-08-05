/*
 * Westmalle Wayland Compositor.
 * Copyright (C) 2016  Erik De Rijcke
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.westmalle.wayland.nativ.libpng;

import org.freedesktop.jaccall.Lib;
import org.freedesktop.jaccall.Pointer;
import org.freedesktop.jaccall.Ptr;
import org.freedesktop.jaccall.Unsigned;

@Lib(value = "png16",
     version = 16)
public class Libpng {

    public static final int PNG_COLOR_MASK_COLOR = 2;
    public static final int PNG_COLOR_MASK_ALPHA = 4;

    public static final int PNG_COLOR_TYPE_RGB_ALPHA     = PNG_COLOR_MASK_COLOR | PNG_COLOR_MASK_ALPHA;
    public static final int PNG_COLOR_TYPE_RGBA          = PNG_COLOR_TYPE_RGB_ALPHA;
    public static final int PNG_INTERLACE_NONE           = 0;
    public static final int PNG_COMPRESSION_TYPE_BASE    = 0;
    public static final int PNG_COMPRESSION_TYPE_DEFAULT = PNG_COMPRESSION_TYPE_BASE;
    public static final int PNG_FILTER_TYPE_BASE         = 0;
    public static final int PNG_FILTER_TYPE_DEFAULT      = PNG_FILTER_TYPE_BASE;
    public static final int PNG_TRANSFORM_IDENTITY       = 0x0000;

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

    public native void png_destroy_write_struct(@Ptr long png_ptr_ptr,
                                                @Ptr long info_ptr_ptr);

//    @Ptr
//    public native long png_jmpbuf(@Ptr long png_ptr);
}
