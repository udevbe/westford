package org.westmalle.wayland.nativ.libinput;


import org.freedesktop.jaccall.Functor;
import org.freedesktop.jaccall.Ptr;

@Functor
@FunctionalInterface
public interface close_restricted {
    void $(int fd, @Ptr(Void.class) long user_data);
}
