package org.westmalle.wayland.dispmanx;


import org.westmalle.wayland.nativ.libbcm_host.EGL_DISPMANX_WINDOW_T;

import javax.inject.Inject;

public class DispmanxEglOutputFactory {

    @Inject
    DispmanxEglOutputFactory() {
    }

    public DispmanxEglOutput create(final EGL_DISPMANX_WINDOW_T dispmanxWindow) {
        return null;
    }
}
