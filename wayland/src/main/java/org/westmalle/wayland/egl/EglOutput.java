package org.westmalle.wayland.egl;

import com.sun.jna.Pointer;

public interface EglOutput {
    Pointer getEglSurface();
    Pointer getEglDisplay();
    void begin();
    void end();
}
