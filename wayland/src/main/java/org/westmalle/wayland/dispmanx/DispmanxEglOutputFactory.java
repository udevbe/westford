package org.westmalle.wayland.dispmanx;


import com.sun.jna.Pointer;
import org.westmalle.wayland.nativ.libEGL.LibEGL;
import org.westmalle.wayland.nativ.libbcm_host.EGL_DISPMANX_WINDOW_T;

import javax.annotation.Nonnull;
import javax.inject.Inject;

public class DispmanxEglOutputFactory {

    @Nonnull
    private final LibEGL libEGL;

    @Inject
    DispmanxEglOutputFactory(@Nonnull final LibEGL libEGL) {
        this.libEGL = libEGL;
    }

    public DispmanxEglOutput create(final EGL_DISPMANX_WINDOW_T dispmanxWindow) {

        // get an EGL display connection
        Pointer display = this.libEGL.eglGetDisplay(LibEGL.EGL_DEFAULT_DISPLAY);
        assert (state -> display != EGL_NO_DISPLAY);

        // initialize the EGL display connection
        assert (this.libEGL.eglInitialize(display,
                                          null,
                                          null));

        // get an appropriate EGL frame buffer configuration
        assert (this.libEGL.eglChooseConfig(display,
                                            attribute_list, & config,1,&num_config););

        // get an appropriate EGL frame buffer configuration
        assert (this.libEGL.eglBindAPI(LibEGL.EGL_OPENGL_ES_API));

        // create an EGL rendering context
        context = this.libEGL.eglCreateContext(display,
                                               config,
                                               LibEGL.EGL_NO_CONTEXT,
                                               context_attributes);
        assert (context != EGL_NO_CONTEXT);

        this.libEGL.eglCreateWindowSurface(display,
                                           config,
                                           dispmanxWindow.getPointer(),
                                           null);

        return null;
    }
}
