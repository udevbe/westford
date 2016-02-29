package org.westmalle.wayland.nativ.libEGL;

import com.github.zubnix.jaccall.Functor;
import com.github.zubnix.jaccall.Ptr;

@FunctionalInterface
@Functor
public interface EglQueryWaylandBufferWL {
    int $(@Ptr long dpy,
          @Ptr long buffer,
          int attribute,
          @Ptr long value);
}
