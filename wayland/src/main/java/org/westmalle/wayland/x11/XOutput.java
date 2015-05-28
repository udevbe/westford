package org.westmalle.wayland.x11;

import com.google.auto.value.AutoValue;

import com.sun.jna.Pointer;

import org.westmalle.wayland.egl.EglOutput;
import org.westmalle.wayland.nativ.Libegl;

@AutoValue
public abstract class XOutput implements EglOutput {
    public static XOutput create(Integer window, Pointer display){
        return new AutoValue_XOutput(window, display);
    }

    public abstract Pointer getEglSurface();
    public abstract Pointer getEglDisplay();
    public void begin(){

    }

    public void end(){

    }
}
