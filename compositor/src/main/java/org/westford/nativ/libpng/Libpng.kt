/*
 * Westford Wayland Compositor.
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
package org.westford.nativ.libpng

import org.freedesktop.jaccall.Lib
import org.freedesktop.jaccall.Pointer
import org.freedesktop.jaccall.Ptr
import org.freedesktop.jaccall.Unsigned

@Lib(value = "png16", version = 16)
class Libpng {

    @Ptr
    external fun png_create_write_struct(@Ptr(Char::class) user_png_ver: Long,
                                         @Ptr(Void::class) error_ptr: Long,
                                         @Ptr(png_error_ptr::class) error_fn: Long,
                                         @Ptr(png_error_ptr::class) warn_fn: Long): Long

    @Ptr
    external fun png_create_info_struct(@Ptr png_ptr: Long): Long

    external fun png_set_IHDR(@Ptr png_ptr: Long,
                              @Ptr info_ptr: Long,
                              @Unsigned width: Int,
                              @Unsigned height: Int,
                              bit_depth: Int,
                              color_type: Int,
                              interlace_type: Int,
                              compression_type: Int,
                              filter_type: Int)

    external fun png_set_rows(@Ptr png_ptr: Long,
                              @Ptr info_ptr: Long,
                              @Ptr(Pointer<*>::class) /*byte*/ row_pointers: Long)

    external fun png_set_write_fn(@Ptr png_ptr: Long,
                                  @Ptr(Void::class) io_ptr: Long,
                                  @Ptr(png_rw_ptr::class) write_data_fn: Long,
                                  @Ptr(png_flush_ptr::class) output_flush_fn: Long)

    external fun png_write_png(@Ptr png_ptr: Long,
                               @Ptr info_ptr: Long,
                               transforms: Int,
                               @Ptr(Void::class) params: Long)

    external fun png_destroy_write_struct(@Ptr png_ptr_ptr: Long,
                                          @Ptr info_ptr_ptr: Long)

    companion object {

        val PNG_COLOR_MASK_COLOR = 2
        val PNG_COLOR_MASK_ALPHA = 4

        val PNG_COLOR_TYPE_RGB_ALPHA = PNG_COLOR_MASK_COLOR or PNG_COLOR_MASK_ALPHA
        val PNG_COLOR_TYPE_RGBA = PNG_COLOR_TYPE_RGB_ALPHA
        val PNG_INTERLACE_NONE = 0
        val PNG_COMPRESSION_TYPE_BASE = 0
        val PNG_COMPRESSION_TYPE_DEFAULT = PNG_COMPRESSION_TYPE_BASE
        val PNG_FILTER_TYPE_BASE = 0
        val PNG_FILTER_TYPE_DEFAULT = PNG_FILTER_TYPE_BASE
        val PNG_TRANSFORM_IDENTITY = 0x0000
    }

    //    @Ptr
    //    public native long png_jmpbuf(@Ptr long png_ptr);
}
