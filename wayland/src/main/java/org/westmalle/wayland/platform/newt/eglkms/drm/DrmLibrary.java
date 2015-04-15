package org.westmalle.wayland.platform.newt.eglkms.drm;


import com.sun.jna.Library;

public interface DrmLibrary extends Library {
    String JNA_LIBRARY_NAME = "drm";

    drmModeRes drmModeGetResources(int fd);

    drmModeConnector drmModeGetConnector(int fd,
                                         int connector_id);

    void drmModeFreeConnector(drmModeConnector connector);

    drmModeEncoder drmModeGetEncoder(int fd,
                                     int encoder_id);

    void drmModeFreeEncoder(drmModeEncoder encoder);
}
