package org.westmalle.wayland.nativ.libEGL;

import org.freedesktop.jaccall.Functor;
import org.freedesktop.jaccall.Ptr;

@FunctionalInterface
@Functor
public interface EglCreatePlatformWindowSurfaceEXT {
    @Ptr
    long $(@Ptr long dpy,
           @Ptr long config,
           @Ptr long native_window,
           @Ptr long attrib_list);
}
