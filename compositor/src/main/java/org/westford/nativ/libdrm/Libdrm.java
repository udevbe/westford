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
package org.westford.nativ.libdrm;


import org.freedesktop.jaccall.Lib;
import org.freedesktop.jaccall.Ptr;
import org.freedesktop.jaccall.Unsigned;

import javax.inject.Singleton;

@Singleton
@Lib(value = "drm",
     version = 2)
public class Libdrm {
    public static final int DRM_MODE_PAGE_FLIP_EVENT = 0x01;

    public static final int DRM_EVENT_CONTEXT_VERSION = 2;

    public static final int DRM_DISPLAY_MODE_LEN = 32;

    public static final int DRM_MODE_CONNECTED         = 1;
    public static final int DRM_MODE_DISCONNECTED      = 2;
    public static final int DRM_MODE_UNKNOWNCONNECTION = 3;

    public static final int DRM_MODE_SUBPIXEL_UNKNOWN        = 1;
    public static final int DRM_MODE_SUBPIXEL_HORIZONTAL_RGB = 2;
    public static final int DRM_MODE_SUBPIXEL_HORIZONTAL_BGR = 3;
    public static final int DRM_MODE_SUBPIXEL_VERTICAL_RGB   = 4;
    public static final int DRM_MODE_SUBPIXEL_VERTICAL_BGR   = 5;
    public static final int DRM_MODE_SUBPIXEL_NONE           = 6;

    public native int drmOpen(@Ptr(String.class) long name,
                              @Ptr(String.class) long busid);

    @Ptr(DrmModeRes.class)
    public native long drmModeGetResources(int fd);

    @Ptr(DrmModeConnector.class)
    public native long drmModeGetConnector(int fd,
                                           @Unsigned int connectorId);

    public native void drmModeFreeConnector(@Ptr(DrmModeConnector.class) long ptr);

    @Ptr(DrmModeEncoder.class)
    public native long drmModeGetEncoder(int fd,
                                         @Unsigned int encoder_id);

    public native void drmModeFreeEncoder(@Ptr(DrmModeEncoder.class) long ptr);

    public native int drmModeRmFB(int fd,
                                  @Unsigned int bufferId);

    public native int drmModeAddFB(int fd,
                                   @Unsigned int width,
                                   @Unsigned int height,
                                   @Unsigned byte depth,
                                   @Unsigned byte bpp,
                                   @Unsigned int pitch,
                                   @Unsigned int bo_handle,
                                   @Ptr(int.class) long buf_id);

    public native int drmModeSetCrtc(int fd,
                                     @Unsigned int crtcId,
                                     @Unsigned int bufferId,
                                     @Unsigned int x,
                                     @Unsigned int y,
                                     @Ptr(int.class) long connectors,
                                     int count,
                                     @Ptr(DrmModeModeInfo.class) long mode);

    public native int drmModePageFlip(int fd,
                                      @Unsigned int crtc_id,
                                      @Unsigned int fb_id,
                                      @Unsigned int flags,
                                      @Ptr long user_data);

    public native int drmHandleEvent(int fd,
                                     @Ptr(DrmEventContext.class) long evctx);

    public native int drmSetMaster(int fd);

    public native int drmDropMaster(int fd);
}
