package org.westmalle.wayland.nativ.libEGL;

import org.freedesktop.jaccall.Functor;
import org.freedesktop.jaccall.Ptr;

@FunctionalInterface
@Functor
public interface EglUnbindWaylandDisplayWL {
    int $(@Ptr long dpy,
          @Ptr long display);
}
