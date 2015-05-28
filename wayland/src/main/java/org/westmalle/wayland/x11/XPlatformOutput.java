package org.westmalle.wayland.x11;

import com.google.auto.value.AutoValue;

import com.sun.jna.Pointer;

import org.westmalle.wayland.egl.PlatformOutput;
import org.westmalle.wayland.nativ.Libegl;

@AutoValue
public abstract class XPlatformOutput implements PlatformOutput{
    public static XPlatformOutput create(Integer window, Pointer display){
        return new AutoValue_XPlatformOutput(window, display);
    }

    @Override
    public abstract Integer getSurface();

    @Override
    public abstract Pointer getDisplay();

    @Override
    public int getPlatform() {
        return Libegl.EGL_PLATFORM_X11_KHR;
    }
}
