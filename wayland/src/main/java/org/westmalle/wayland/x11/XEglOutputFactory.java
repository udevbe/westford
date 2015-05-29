package org.westmalle.wayland.x11;


import com.sun.jna.Pointer;

import org.westmalle.wayland.nativ.Libegl;

import javax.annotation.Nonnull;
import javax.inject.Inject;

public class XEglOutputFactory {

    @Nonnull
    private final Libegl libegl;

    @Inject
    XEglOutputFactory(@Nonnull final Libegl libegl) {
        this.libegl = libegl;
    }

    public XEglOutput create(final Pointer display,
                             final int window) {
        final Pointer eglDisplay = this.libegl.eglGetPlatformDisplayEXT(Libegl.EGL_PLATFORM_X11_KHR,
                                                                        display,
                                                                        null);
        final Pointer eglSurface = this.libegl.eglCreatePlatformWindowSurfaceEXT(eglDisplay,
                                                                                 null,
                                                                                 Pointer.createConstant(window),
                                                                                 null);
        final Pointer eglContext = this.libegl.eglCreateContext(eglDisplay,
                                                                null,
                                                                null,
                                                                null);
        this.libegl.eglMakeCurrent(display,
                                   eglSurface,
                                   eglSurface,
                                   eglContext);
        return new XEglOutput(this.libegl,
                              eglDisplay,
                              eglSurface);
    }
}
