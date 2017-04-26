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
package org.westford.nativ.libpixman1

import org.freedesktop.jaccall.Lib
import org.freedesktop.jaccall.Ptr

import javax.inject.Singleton

@Singleton
@Lib(value = "pixman-1", version = 0)
class Libpixman1 {

    @Ptr
    external fun pixman_region32_rectangles(@Ptr region: Long,
                                            @Ptr n_rects: Long): Long

    external fun pixman_region32_union_rect(@Ptr dest: Long,
                                            @Ptr source: Long,
                                            x: Int,
                                            y: Int,
                                            width: Int,
                                            height: Int): Int

    external fun pixman_region32_union(@Ptr new_reg: Long,
                                       @Ptr reg1: Long,
                                       @Ptr reg2: Long): Int


    external fun pixman_region32_init_rect(@Ptr region: Long,
                                           x: Int,
                                           y: Int,
                                           width: Int,
                                           height: Int)

    external fun pixman_region32_subtract(@Ptr reg_d: Long,
                                          @Ptr reg_m: Long,
                                          @Ptr reg_s: Long): Int

    external fun pixman_region32_contains_point(@Ptr region: Long,
                                                x: Int,
                                                y: Int,
                                                @Ptr box: Long): Int

    external fun pixman_region32_intersect_rect(@Ptr dest: Long,
                                                @Ptr source: Long,
                                                x: Int,
                                                y: Int,
                                                width: Int,
                                                height: Int): Int

    external fun pixman_region32_contains_rectangle(@Ptr region: Long,
                                                    @Ptr prect: Long): Int

    external fun pixman_region32_not_empty(@Ptr region: Long): Int


    external fun pixman_region32_init(@Ptr region: Long)

    external fun pixman_region32_clear(@Ptr region: Long)

    external fun pixman_region32_fini(@Ptr region: Long)

    external fun pixman_region32_copy(@Ptr dest: Long,
                                      @Ptr source: Long): Int

    companion object {

        val PIXMAN_REGION_OUT = 0
        val PIXMAN_REGION_IN = 1
        val PIXMAN_REGION_PART = 2
    }
}
