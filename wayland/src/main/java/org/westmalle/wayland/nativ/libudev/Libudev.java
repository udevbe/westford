package org.westmalle.wayland.nativ.libudev;

import org.freedesktop.jaccall.Lib;
import org.freedesktop.jaccall.Ptr;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@Lib("udev")
public class Libudev {

    @Inject
    Libudev() {
    }

    @Ptr
    public native long udev_unref(@Ptr long udev);

    @Ptr
    public native long udev_new();

}
