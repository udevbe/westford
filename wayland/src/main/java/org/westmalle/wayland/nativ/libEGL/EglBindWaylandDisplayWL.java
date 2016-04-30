package org.westmalle.wayland.nativ.libEGL;


import org.freedesktop.jaccall.Functor;
import org.freedesktop.jaccall.Ptr;

@FunctionalInterface
@Functor
public interface EglBindWaylandDisplayWL {
    int $(@Ptr long dpy,
          @Ptr long wlDisplay);
}
