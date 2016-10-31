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
package org.westford.nativ.libgbm;

import org.freedesktop.jaccall.Lib;
import org.freedesktop.jaccall.Ptr;
import org.freedesktop.jaccall.Unsigned;

@Lib(value = "gbm",
     version = 1)
public class Libgbm {

    /**
     * RGB with 8 bits per channel in a 32 bit value
     */
    public static final int GBM_BO_FORMAT_XRGB8888  = 0;
    /**
     * ARGB with 8 bits per channel in a 32 bit value
     */
    public static final int GBM_BO_FORMAT_ARGB8888  = 1;
    public static final int GBM_FORMAT_XRGB8888     = __gbm_fourcc_code((byte) 'X',
                                                                        (byte) 'R',
                                                                        (byte) '2',
                                                                        (byte) '4');
    /**
     * Buffer is going to be presented to the screen using an API such as KMS
     */
    public static final int GBM_BO_USE_SCANOUT      = (1 << 0);
    /**
     * Buffer is going to be used as cursor
     */
    public static final int GBM_BO_USE_CURSOR       = (1 << 1);
    /**
     * Deprecated
     */
    public static final int GBM_BO_USE_CURSOR_64X64 = GBM_BO_USE_CURSOR;
    /**
     * Buffer is to be used for rendering - for example it is going to be used
     * as the storage for a color buffer
     */
    public static final int GBM_BO_USE_RENDERING    = (1 << 2);
    /**
     * Buffer can be used for gbm_bo_write.  This is guaranteed to work
     * with GBM_BO_USE_CURSOR. but may not work for other combinations.
     */
    public static final int GBM_BO_USE_WRITE        = (1 << 3);
    /**
     * Buffer is linear, i.e. not tiled.
     */
    public static final int GBM_BO_USE_LINEAR       = (1 << 4);

    private static int __gbm_fourcc_code(final byte a,
                                         final byte b,
                                         final byte c,
                                         final byte d) {
        return ((int) (a) | ((int) (b) << 8) | ((int) (c) << 16) | ((int) (d) << 24));
    }

    @Ptr
    public native long gbm_create_device(int fd);

    @Ptr
    public native long gbm_surface_create(@Ptr long gbm,
                                          @Unsigned int width,
                                          @Unsigned int height,
                                          @Unsigned int format,
                                          @Unsigned int flags);

    @Ptr
    public native long gbm_bo_get_user_data(@Ptr long bo);

    public native void gbm_bo_set_user_data(@Ptr long bo,
                                            @Ptr long data,
                                            @Ptr(destroy_user_data.class) long destroy_user_data);

    @Ptr
    public native long gbm_bo_get_device(@Ptr long bo);

    @Unsigned
    public native int gbm_bo_get_width(@Ptr long bo);

    @Unsigned
    public native int gbm_bo_get_height(@Ptr long bo);

    @Unsigned
    public native int gbm_bo_get_stride(@Ptr long bo);

    @Unsigned
    public native int gbm_bo_get_format(@Ptr long bo);

    public native long gbm_bo_get_handle(@Ptr long bo);

    @Ptr
    public native long gbm_surface_lock_front_buffer(@Ptr long surface);

    public native void gbm_surface_release_buffer(@Ptr long surface,
                                                  @Ptr long bo);
}
