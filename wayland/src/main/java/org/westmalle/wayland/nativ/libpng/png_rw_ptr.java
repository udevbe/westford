package org.westmalle.wayland.nativ.libpng;

import org.freedesktop.jaccall.Functor;
import org.freedesktop.jaccall.Lng;
import org.freedesktop.jaccall.Ptr;
import org.freedesktop.jaccall.Unsigned;

@Functor
@FunctionalInterface
public interface png_rw_ptr {

    void $(@Ptr long png_structp,
           @Ptr(byte.class) long png_bytep,
           @Unsigned @Lng long png_size_t);
}
