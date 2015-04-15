package org.westmalle.wayland.platform.newt.eglkms.drm;

public interface drmModeSubPixel {
    int DRM_MODE_SUBPIXEL_UNKNOWN        = 1;
    int DRM_MODE_SUBPIXEL_HORIZONTAL_RGB = 2;
    int DRM_MODE_SUBPIXEL_HORIZONTAL_BGR = 3;
    int DRM_MODE_SUBPIXEL_VERTICAL_RGB   = 4;
    int DRM_MODE_SUBPIXEL_VERTICAL_BGR   = 5;
    int DRM_MODE_SUBPIXEL_NONE           = 6;
}
