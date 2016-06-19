package org.westmalle.wayland.nativ.libEGL;

import org.freedesktop.jaccall.Functor;
import org.freedesktop.jaccall.Ptr;

@Functor
@FunctionalInterface
public interface EglCreateImageKHR {

    @Ptr
    long $(@Ptr long dpy,
           @Ptr long ctx,
           int target,
           @Ptr long buffer,
           @Ptr(Integer.class) long attrib_list);
}
