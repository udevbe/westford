package org.westmalle.wayland.platform.newt.eglkms.gbm;


import com.sun.jna.Native;
import com.sun.jna.Pointer;

public interface GbmLibrary {
    String JNA_LIBRARY_NAME = "gbm";

    GbmLibrary INSTANCE = (GbmLibrary) Native.loadLibrary(JNA_LIBRARY_NAME, GbmLibrary.class);

    Pointer gbm_create_device(int fd);

    Pointer gbm_bo_create (Pointer gbm, int width, int height, int format, int usage);
}
