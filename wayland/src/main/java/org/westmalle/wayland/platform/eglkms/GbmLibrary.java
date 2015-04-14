package org.westmalle.wayland.platform.eglkms;


import com.sun.jna.Pointer;

public interface GbmLibrary {
    String JNA_LIBRARY_NAME = "gbm";

    Pointer gbm_create_device(int fd);
}
