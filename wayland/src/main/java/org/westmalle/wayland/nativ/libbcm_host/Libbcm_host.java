package org.westmalle.wayland.nativ.libbcm_host;

import com.sun.jna.Pointer;

import javax.inject.Singleton;

@Singleton
public class Libbcm_host {

    public static final int DISPMANX_FLAGS_ALPHA_FROM_SOURCE       = 0;
    public static final int DISPMANX_FLAGS_ALPHA_FIXED_ALL_PIXELS  = 1;
    public static final int DISPMANX_FLAGS_ALPHA_FIXED_NON_ZERO    = 2;
    public static final int DISPMANX_FLAGS_ALPHA_FIXED_EXCEED_0X07 = 3;

    public static final int DISPMANX_FLAGS_ALPHA_PREMULT = 1 << 16;
    public static final int DISPMANX_FLAGS_ALPHA_MIX     = 1 << 17;

    public static final int DISPMANX_NO_ROTATE  = 0;
    public static final int DISPMANX_ROTATE_90  = 1;
    public static final int DISPMANX_ROTATE_180 = 2;
    public static final int DISPMANX_ROTATE_270 = 3;

    public static final int DISPMANX_FLIP_HRIZ = 1 << 16;
    public static final int DISPMANX_FLIP_VERT = 1 << 17;

    public native void bcm_host_init();

    public native int graphics_get_display_size(short display_number,
                                                Pointer display_width,
                                                Pointer display_height);

    /**
     * Opens a display on the given device
     *
     * @param device
     *
     * @return
     */
    public native int vc_dispmanx_display_open(int device);

    /**
     * Start a new update, DISPMANX_NO_HANDLE on error
     *
     * @param priority
     *
     * @return
     */
    public native int vc_dispmanx_update_start(int priority);

    /**
     * Add an elment to a display as part of an update
     */
    public native int vc_dispmanx_element_add(int update,
                                              int display,
                                              int layer,
                                              Pointer dest_rect,
                                              int src,
                                              Pointer src_rect,
                                              int protection,
                                              Pointer alpha,
                                              Pointer clamp,
                                              int transform);

    /**
     * End an update and wait for it to complete
     */
    public native int vc_dispmanx_update_submit_sync(int update);
}
