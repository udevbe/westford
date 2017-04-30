/*
 * Westford Wayland Compositor.
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
package org.westford.nativ.libbcm_host

import org.freedesktop.jaccall.Lib
import org.freedesktop.jaccall.Ptr

import javax.inject.Singleton

@Singleton @Lib("bcm_host") class Libbcm_host {

    external fun bcm_host_init()

    external fun vc_dispmanx_display_get_info(display: Int,
                                              @Ptr pinfo: Long): Int

    /**
     * Opens a display on the given device

     * @param device c
     * *
     * *
     * @return
     */
    external fun vc_dispmanx_display_open(device: Int): Int

    /**
     * Start a new update, DISPMANX_NO_HANDLE on error

     * @param priority
     * *
     * *
     * @return
     */
    external fun vc_dispmanx_update_start(priority: Int): Int

    /**
     * Add an elment to a display as part of an update
     */
    external fun vc_dispmanx_element_add(update: Int,
                                         display: Int,
                                         layer: Int,
                                         @Ptr dest_rect: Long,
                                         src: Int,
                                         @Ptr src_rect: Long,
                                         protection: Int,
                                         @Ptr alpha: Long,
                                         @Ptr clamp: Long,
                                         transform: Int): Int

    /**
     * End an update and wait for it to complete
     */
    external fun vc_dispmanx_update_submit_sync(update: Int): Int

    external fun vc_dispmanx_rect_set(@Ptr rect: Long,
                                      x_offset: Int,
                                      y_offset: Int,
                                      width: Int,
                                      height: Int): Int

    external fun graphics_get_display_size(display_number: Int,
                                           @Ptr width: Long,
                                           @Ptr height: Long): Int

    companion object {

        val DISPMANX_ID_MAIN_LCD = 0
        val DISPMANX_ID_AUX_LCD = 1
        val DISPMANX_ID_HDMI = 2
        val DISPMANX_ID_SDTV = 3
        val DISPMANX_ID_FORCE_LCD = 4
        val DISPMANX_ID_FORCE_TV = 5
        val DISPMANX_ID_FORCE_OTHER = 6/* non-default display */

        val DISPMANX_PROTECTION_MAX = 0x0f
        val DISPMANX_PROTECTION_NONE = 0
        val DISPMANX_PROTECTION_HDCP = 11

        val DISPMANX_FLAGS_ALPHA_FROM_SOURCE = 0
        val DISPMANX_FLAGS_ALPHA_FIXED_ALL_PIXELS = 1
        val DISPMANX_FLAGS_ALPHA_FIXED_NON_ZERO = 2
        val DISPMANX_FLAGS_ALPHA_FIXED_EXCEED_0X07 = 3

        val DISPMANX_FLAGS_ALPHA_PREMULT = 1 shl 16
        val DISPMANX_FLAGS_ALPHA_MIX = 1 shl 17

        val DISPMANX_NO_ROTATE = 0
        val DISPMANX_ROTATE_90 = 1
        val DISPMANX_ROTATE_180 = 2
        val DISPMANX_ROTATE_270 = 3

        val DISPMANX_FLIP_HRIZ = 1 shl 16
        val DISPMANX_FLIP_VERT = 1 shl 17

        /* invert left/right images */
        val DISPMANX_STEREOSCOPIC_INVERT = 1 shl 19
        /* extra flags for controlling 3d duplication behaviour */
        val DISPMANX_STEREOSCOPIC_NONE = 0 shl 20
        val DISPMANX_STEREOSCOPIC_MONO = 1 shl 20
        val DISPMANX_STEREOSCOPIC_SBS = 2 shl 20
        val DISPMANX_STEREOSCOPIC_TB = 3 shl 20
        val DISPMANX_STEREOSCOPIC_MASK = 15 shl 20

        /* extra flags for controlling snapshot behaviour */
        val DISPMANX_SNAPSHOT_NO_YUV = 1 shl 24
        val DISPMANX_SNAPSHOT_NO_RGB = 1 shl 25
        val DISPMANX_SNAPSHOT_FILL = 1 shl 26
        val DISPMANX_SNAPSHOT_SWAP_RED_BLUE = 1 shl 27
        val DISPMANX_SNAPSHOT_PACK = 1 shl 28

        val VCOS_DISPLAY_INPUT_FORMAT_INVALID = 0
        val VCOS_DISPLAY_INPUT_FORMAT_RGB888 = 1
        val VCOS_DISPLAY_INPUT_FORMAT_RGB565 = 2
    }
}
