package org.westmalle.wayland.nativ.libEGL;

import com.github.zubnix.jaccall.Functor;
import com.github.zubnix.jaccall.Ptr;

@FunctionalInterface
@Functor
public interface EglUnbindWaylandDisplayWL {
    int $(@Ptr long dpy,
          @Ptr long display);
}
