package org.westmalle.wayland.nativ.libEGL;


import org.freedesktop.jaccall.Functor;
import org.freedesktop.jaccall.Ptr;

@Functor
@FunctionalInterface
public interface EglDestroyImageKHR {
    int $(@Ptr long dpy,
          @Ptr long image);
}
