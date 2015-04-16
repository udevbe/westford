package org.westmalle.wayland.platform.newt.eglkms.gbm;


public interface GBM_BO {

    /**
     * Buffer is going to be presented to the screen using an API such as KMS
     */
    int GBM_BO_USE_SCANOUT = (1 << 0);
    /**
     * Buffer is going to be used as cursor
     */
    int GBM_BO_USE_CURSOR = (1 << 1);
    /**
     * Deprecated
     */
    int GBM_BO_USE_CURSOR_64X64 = GBM_BO_USE_CURSOR;
    /**
     * Buffer is to be used for rendering - for example it is going to be used as the storage for a color buffer
     */
    int GBM_BO_USE_RENDERING = (1 << 2);
    /**
     * Buffer can be used for gbm_bo_write.  This is guaranteed to work with GBM_BO_USE_CURSOR. but may not work for
     * other combinations.
     */
    int GBM_BO_USE_WRITE = (1 << 3);
}
