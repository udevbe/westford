package org.westmalle.wayland.nativ.libpng;

import org.freedesktop.jaccall.Functor;
import org.freedesktop.jaccall.Ptr;

@FunctionalInterface
@Functor
public interface png_error_ptr {
    void $(@Ptr long png_structp,
           @Ptr(byte.class) long png_const_charp);
}
