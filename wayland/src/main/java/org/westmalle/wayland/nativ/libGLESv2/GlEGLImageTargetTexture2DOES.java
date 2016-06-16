package org.westmalle.wayland.nativ.libGLESv2;

import org.freedesktop.jaccall.Functor;
import org.freedesktop.jaccall.Ptr;
import org.freedesktop.jaccall.Unsigned;

@FunctionalInterface
@Functor
public interface GlEGLImageTargetTexture2DOES {
    void $(@Unsigned int target,
           @Ptr(Void.class) long image);
}
