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
package org.westford.nativ.libpixman1;

import org.freedesktop.jaccall.Lib;
import org.freedesktop.jaccall.Ptr;

import javax.inject.Singleton;

@Singleton
@Lib(value = "pixman-1",
     version = 0)
public class Libpixman1 {

    public static final int PIXMAN_REGION_OUT  = 0;
    public static final int PIXMAN_REGION_IN   = 1;
    public static final int PIXMAN_REGION_PART = 2;

    @Ptr
    public native long pixman_region32_rectangles(@Ptr long region,
                                                  @Ptr long n_rects);

    public native int pixman_region32_union_rect(@Ptr long dest,
                                                 @Ptr long source,
                                                 int x,
                                                 int y,
                                                 int width,
                                                 int height);

    public native int pixman_region32_union(@Ptr long new_reg,
                                            @Ptr long reg1,
                                            @Ptr long reg2);


    public native void pixman_region32_init_rect(@Ptr long region,
                                                 int x,
                                                 int y,
                                                 int width,
                                                 int height);

    public native int pixman_region32_subtract(@Ptr long reg_d,
                                               @Ptr long reg_m,
                                               @Ptr long reg_s);

    public native int pixman_region32_contains_point(@Ptr long region,
                                                     int x,
                                                     int y,
                                                     @Ptr long box);

    public native int pixman_region32_intersect_rect(@Ptr long dest,
                                                     @Ptr long source,
                                                     int x,
                                                     int y,
                                                     int width,
                                                     int height);

    public native int pixman_region32_contains_rectangle(@Ptr long region,
                                                         @Ptr long prect);

    public native void pixman_region32_init(@Ptr long region);

    public native void pixman_region32_clear(@Ptr long region);
}
