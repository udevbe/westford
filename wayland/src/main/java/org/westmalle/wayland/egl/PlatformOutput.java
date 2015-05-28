package org.westmalle.wayland.egl;

public interface PlatformOutput {
    Object getSurface();
    Object getDisplay();

    /**
     * Any of the EGL_PLATFORM_* constants.
     * @return
     */
    int getPlatform();
}
