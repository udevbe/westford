package org.westmalle.wayland.nativ.libEGL;

import org.freedesktop.jaccall.Functor;
import org.freedesktop.jaccall.Ptr;

@FunctionalInterface
@Functor
public interface EglGetPlatformDisplayEXT {
    @Ptr
    long $(int platform,
           @Ptr long native_display,
           @Ptr long attrib_list);
}
