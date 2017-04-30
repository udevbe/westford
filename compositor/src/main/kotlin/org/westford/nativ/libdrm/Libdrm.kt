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
package org.westford.nativ.libdrm

import org.freedesktop.jaccall.Lib
import org.freedesktop.jaccall.Ptr
import org.freedesktop.jaccall.Unsigned

import javax.inject.Singleton

@Singleton @Lib(value = "drm",
                version = 2) class Libdrm {

    /**
     * Open the DRM device.
     *
     *
     * Looks up the specified name and bus ID, and opens the device found.  The
     * entry in /dev/dri is created if necessary and if called by root.

     * @param name  driver name. Not referenced if bus ID is supplied.
     * *
     * @param busid bus ID. Zero if not known.
     * *
     * *
     * @return a file descriptor on success, or a negative value on error.
     */
    external fun drmOpen(@Ptr(String::class) name: Long,
                         @Ptr(String::class) busid: Long): Int

    /**
     * Retrieves all of the resources associated with a card.
     */
    @Ptr(DrmModeRes::class) external fun drmModeGetResources(fd: Int): Long

    /**
     * Retrieve information about the connector connectorId.
     */
    @Ptr(DrmModeConnector::class) external fun drmModeGetConnector(fd: Int,
                                                                   @Unsigned connectorId: Int): Long

    external fun drmModeFreeConnector(@Ptr(DrmModeConnector::class) ptr: Long)

    @Ptr(DrmModeEncoder::class) external fun drmModeGetEncoder(fd: Int,
                                                               @Unsigned encoder_id: Int): Long

    external fun drmModeFreeEncoder(@Ptr(DrmModeEncoder::class) ptr: Long)

    /**
     * Destroys the given framebuffer.
     */
    external fun drmModeRmFB(fd: Int,
                             @Unsigned bufferId: Int): Int

    /**
     * Creates a new framebuffer with an buffer object as its scanout buffer.
     */
    external fun drmModeAddFB(fd: Int,
                              @Unsigned width: Int,
                              @Unsigned height: Int,
                              @Unsigned depth: Byte,
                              @Unsigned bpp: Byte,
                              @Unsigned pitch: Int,
                              @Unsigned bo_handle: Int,
                              @Ptr(Int::class) buf_id: Long): Int

    external fun drmModeAddFB2(fd: Int,
                               @Unsigned width: Int,
                               @Unsigned height: Int,
                               @Unsigned pixel_format: Int,
                               @Ptr(Int::class) bo_handles: Long,
                               @Ptr(Int::class) pitches: Long,
                               @Ptr(Int::class) offsets: Long,
                               @Ptr(Int::class) buf_id: Long,
                               @Unsigned flags: Int): Int

    /**
     * Set the mode on a crtc crtcId with the given mode modeId.
     */
    external fun drmModeSetCrtc(fd: Int,
                                @Unsigned crtcId: Int,
                                @Unsigned bufferId: Int,
                                @Unsigned x: Int,
                                @Unsigned y: Int,
                                @Ptr(Int::class) connectors: Long,
                                count: Int,
                                @Ptr(DrmModeModeInfo::class) mode: Long): Int

    external fun drmModePageFlip(fd: Int,
                                 @Unsigned crtc_id: Int,
                                 @Unsigned fb_id: Int,
                                 @Unsigned flags: Int,
                                 @Ptr user_data: Long): Int

    external fun drmHandleEvent(fd: Int,
                                @Ptr(DrmEventContext::class) evctx: Long): Int

    external fun drmSetMaster(fd: Int): Int

    external fun drmDropMaster(fd: Int): Int

    external fun drmModeSetPlane(fd: Int,
                                 @Unsigned plane_id: Int,
                                 @Unsigned crtc_id: Int,
                                 @Unsigned fb_id: Int,
                                 @Unsigned flags: Int,
                                 @Unsigned crtc_x: Int,
                                 @Unsigned crtc_y: Int,
                                 @Unsigned crtc_w: Int,
                                 @Unsigned crtc_h: Int,
                                 @Unsigned src_x: Int,
                                 @Unsigned src_y: Int,
                                 @Unsigned src_w: Int,
                                 @Unsigned src_h: Int): Int

    @Ptr(drmModePlaneRes::class) external fun drmModeGetPlaneResources(fd: Int): Long

    external fun drmModeFreePlaneResources(@Ptr(drmModePlaneRes::class) ptr: Long)

    @Ptr(drmModePlane::class) external fun drmModeGetPlane(fd: Int,
                                                           @Unsigned plane_id: Int): Long

    external fun drmModeFreePlane(@Ptr(drmModePlane::class) ptr: Long)

    companion object {
        val DRM_MODE_PAGE_FLIP_EVENT = 0x01

        val DRM_EVENT_CONTEXT_VERSION = 2

        const val DRM_DISPLAY_MODE_LEN = 32

        val DRM_MODE_CONNECTED = 1
        val DRM_MODE_DISCONNECTED = 2
        val DRM_MODE_UNKNOWNCONNECTION = 3

        val DRM_MODE_SUBPIXEL_UNKNOWN = 1
        val DRM_MODE_SUBPIXEL_HORIZONTAL_RGB = 2
        val DRM_MODE_SUBPIXEL_HORIZONTAL_BGR = 3
        val DRM_MODE_SUBPIXEL_VERTICAL_RGB = 4
        val DRM_MODE_SUBPIXEL_VERTICAL_BGR = 5
        val DRM_MODE_SUBPIXEL_NONE = 6
    }
}
