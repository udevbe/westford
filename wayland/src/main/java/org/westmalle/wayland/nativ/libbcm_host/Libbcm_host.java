/*
 * Westmalle Wayland Compositor.
 * Copyright (C) 2016  Erik De Rijcke
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.westmalle.wayland.nativ.libbcm_host;

import org.freedesktop.jaccall.Lib;
import org.freedesktop.jaccall.Ptr;

import javax.inject.Singleton;

@Singleton
@Lib("bcm_host")
public class Libbcm_host {

    public static final int DISPMANX_ID_MAIN_LCD    = 0;
    public static final int DISPMANX_ID_AUX_LCD     = 1;
    public static final int DISPMANX_ID_HDMI        = 2;
    public static final int DISPMANX_ID_SDTV        = 3;
    public static final int DISPMANX_ID_FORCE_LCD   = 4;
    public static final int DISPMANX_ID_FORCE_TV    = 5;
    public static final int DISPMANX_ID_FORCE_OTHER = 6 /* non-default display */;

    public static final int DISPMANX_PROTECTION_MAX  = 0x0f;
    public static final int DISPMANX_PROTECTION_NONE = 0;
    public static final int DISPMANX_PROTECTION_HDCP = 11;

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

    /* invert left/right images */
    public static final int DISPMANX_STEREOSCOPIC_INVERT = 1 << 19;
    /* extra flags for controlling 3d duplication behaviour */
    public static final int DISPMANX_STEREOSCOPIC_NONE   = 0 << 20;
    public static final int DISPMANX_STEREOSCOPIC_MONO   = 1 << 20;
    public static final int DISPMANX_STEREOSCOPIC_SBS    = 2 << 20;
    public static final int DISPMANX_STEREOSCOPIC_TB     = 3 << 20;
    public static final int DISPMANX_STEREOSCOPIC_MASK   = 15 << 20;

    /* extra flags for controlling snapshot behaviour */
    public static final int DISPMANX_SNAPSHOT_NO_YUV        = 1 << 24;
    public static final int DISPMANX_SNAPSHOT_NO_RGB        = 1 << 25;
    public static final int DISPMANX_SNAPSHOT_FILL          = 1 << 26;
    public static final int DISPMANX_SNAPSHOT_SWAP_RED_BLUE = 1 << 27;
    public static final int DISPMANX_SNAPSHOT_PACK          = 1 << 28;

    public static final int VCOS_DISPLAY_INPUT_FORMAT_INVALID = 0;
    public static final int VCOS_DISPLAY_INPUT_FORMAT_RGB888  = 1;
    public static final int VCOS_DISPLAY_INPUT_FORMAT_RGB565  = 2;

    public native void bcm_host_init();

    public native int vc_dispmanx_display_get_info(int display,
                                                   @Ptr long pinfo);

    /**
     * Opens a display on the given device
     *
     * @param device c
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
                                              @Ptr long dest_rect,
                                              int src,
                                              @Ptr long src_rect,
                                              int protection,
                                              @Ptr long alpha,
                                              @Ptr long clamp,
                                              int transform);

    /**
     * End an update and wait for it to complete
     */
    public native int vc_dispmanx_update_submit_sync(int update);

    public native int vc_dispmanx_rect_set(@Ptr long rect,
                                           int x_offset,
                                           int y_offset,
                                           int width,
                                           int height);

    public native int graphics_get_display_size(int display_number,
                                                @Ptr long width,
                                                @Ptr long height);
}
