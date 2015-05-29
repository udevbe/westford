package org.westmalle.wayland.x11;

import com.sun.jna.Pointer;
import org.westmalle.wayland.egl.EglOutput;
import org.westmalle.wayland.nativ.Libegl;

import javax.annotation.Nonnull;


public class XEglOutput implements EglOutput {

    @Nonnull
    private final Libegl  libegl;
    @Nonnull
    private final Pointer eglDisplay;
    @Nonnull
    private final Pointer eglSurface;
    @Nonnull
    private final Pointer eglContext;

    XEglOutput(@Nonnull final Libegl libegl,
               @Nonnull final Pointer eglDisplay,
               @Nonnull final Pointer eglSurface,
               @Nonnull final Pointer eglContext) {
        this.libegl = libegl;
        this.eglDisplay = eglDisplay;
        this.eglSurface = eglSurface;
        this.eglContext = eglContext;
    }

    @Override
    public void begin() {
        this.libegl.eglMakeCurrent(this.eglDisplay,
                                   this.eglSurface,
                                   this.eglSurface,
                                   this.eglContext);
    }

    @Override
    public void end() {
        this.libegl.eglSwapBuffers(this.eglDisplay,
                                   this.eglSurface);
    }
}
