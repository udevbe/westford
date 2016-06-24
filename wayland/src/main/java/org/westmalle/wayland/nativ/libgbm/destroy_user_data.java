package org.westmalle.wayland.nativ.libgbm;

import org.freedesktop.jaccall.Functor;
import org.freedesktop.jaccall.Ptr;

@Functor
@FunctionalInterface
public interface destroy_user_data {
    void $(@Ptr long bo, @Ptr long data);
}
