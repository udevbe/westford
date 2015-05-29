package org.westmalle.wayland.x11;

import com.sun.jna.Pointer;

import org.westmalle.wayland.egl.EglOutput;
import org.westmalle.wayland.nativ.Libegl;

import javax.annotation.Nonnull;


public class XEglOutput implements EglOutput {

    @Nonnull
    private final Libegl libegl;
    @Nonnull
    private final Pointer eglDisplay;
    @Nonnull
    private final Pointer eglSurface;

    XEglOutput(@Nonnull final Libegl libegl,
               @Nonnull final Pointer eglDisplay,
               @Nonnull final Pointer eglSurface) {
        this.libegl = libegl;
        this.eglDisplay = eglDisplay;
        this.eglSurface = eglSurface;
    }

    @Override
    public void end() {
        this.libegl.eglSwapBuffers(this.eglDisplay,
                                   this.eglSurface);
    }
}
