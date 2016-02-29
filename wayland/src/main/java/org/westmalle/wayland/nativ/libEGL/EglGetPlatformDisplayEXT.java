package org.westmalle.wayland.nativ.libEGL;

import com.github.zubnix.jaccall.Functor;
import com.github.zubnix.jaccall.Ptr;

@FunctionalInterface
@Functor
public interface EglGetPlatformDisplayEXT {
    @Ptr
    long $(int platform,
           @Ptr long native_display,
           @Ptr long attrib_list);
}
