package org.westmalle.wayland.nativ;

import com.sun.jna.Native;
import com.sun.jna.ptr.IntByReference;

import javax.inject.Singleton;

@Singleton
public class Libpixman1 {
    static{
        Native.register("pixman-1");
    }

    Libpixman1() {
    }

    public native pixman_box32 pixman_region32_rectangles(pixman_region32 region, IntByReference n_rects);

    public native int pixman_region32_union_rect(pixman_region32 dest, pixman_region32 source, int x, int y, int width, int height);

    public native void pixman_region32_init_rect(pixman_region32 region, int x, int y, int width, int height);

    public native int pixman_region32_subtract(pixman_region32 reg_d, pixman_region32 reg_m, pixman_region32 reg_s);

    public native int pixman_region32_contains_point(pixman_region32 region, int x, int y, pixman_box32 box);

    public native int pixman_region32_intersect_rect(pixman_region32 dest, pixman_region32 source, int x, int y, int width, int height);

}
