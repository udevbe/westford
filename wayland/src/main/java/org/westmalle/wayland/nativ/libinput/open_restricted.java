package org.westmalle.wayland.nativ.libinput;

import org.freedesktop.jaccall.Functor;
import org.freedesktop.jaccall.Ptr;

@Functor
@FunctionalInterface
public interface open_restricted {
    int $(@Ptr(String.class) long path, int flags, @Ptr(Void.class) long user_data);
}
