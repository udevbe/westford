package org.westmalle.wayland.nativ.linux;


public class Kdev_t {
    public static int MAJOR(final int dev) {
        return ((dev) >> 8);
    }


    public static int MINOR(final int dev) {
        return ((dev) & 0xff);
    }
}
