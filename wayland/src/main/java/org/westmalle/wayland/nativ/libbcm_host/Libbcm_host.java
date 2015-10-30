//Copyright 2015 Erik De Rijcke
//
//Licensed under the Apache License,Version2.0(the"License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
//http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing,software
//distributed under the License is distributed on an"AS IS"BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
package org.westmalle.wayland.nativ.libbcm_host;

import com.sun.jna.Pointer;

import javax.inject.Singleton;

@Singleton
public class Libbcm_host {

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
                                                   DISPMANX_MODEINFO_T pinfo);

    /**
     * Opens a display on the given device
     *
     * @param device
     *c
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
