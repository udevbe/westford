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
package org.westford.nativ.libEGL

import org.freedesktop.jaccall.Lib
import org.freedesktop.jaccall.Ptr

import javax.inject.Singleton

@Singleton @Lib(value = "EGL",
                version = 1) class LibEGL {

    external fun eglInitialize(@Ptr dpy: Long,
                               @Ptr major: Long,
                               @Ptr minor: Long): Int

    external fun eglTerminate(@Ptr dpy: Long): Int

    external fun eglBindAPI(api: Int): Int

    external fun eglSwapBuffers(@Ptr dpy: Long,
                                @Ptr surface: Long): Int

    external fun eglMakeCurrent(@Ptr dpy: Long,
                                @Ptr draw: Long,
                                @Ptr read: Long,
                                @Ptr context: Long): Int

    @Ptr external fun eglCreateContext(@Ptr dpy: Long,
                                       @Ptr config: Long,
                                       @Ptr share_context: Long,
                                       @Ptr attrib_list: Long): Long

    @Ptr external fun eglGetDisplay(@Ptr native_display: Long): Long

    @Ptr external fun eglCreateWindowSurface(@Ptr display: Long,
                                             @Ptr config: Long,
                                             @Ptr native_window: Long,
                                             @Ptr attrib_list: Long): Long

    @Ptr external fun eglQueryString(@Ptr dpy: Long,
                                     name: Int): Long

    external fun eglChooseConfig(@Ptr dpy: Long,
                                 @Ptr attrib_list: Long,
                                 @Ptr configs: Long,
                                 config_size: Int,
                                 @Ptr num_config: Long): Int

    external fun eglQueryContext(@Ptr dpy: Long,
                                 @Ptr ctx: Long,
                                 attribute: Int,
                                 @Ptr value: Long): Int

    @Ptr external fun eglGetProcAddress(@Ptr procname: Long): Long

    external fun eglGetConfigs(@Ptr display: Long,
                               @Ptr configs: Long,
                               config_size: Int,
                               @Ptr num_config: Long): Int

    external fun eglSurfaceAttrib(@Ptr display: Long,
                                  @Ptr surface: Long,
                                  attribute: Int,
                                  value: Int): Int

    @Throws(RuntimeException::class) fun throwError(failedFunction: String) {
        val eglError = eglGetError()
        when (eglError) {
            EGL_SUCCESS             -> {
                System.err.println(failedFunction + " success.")
            }
            EGL_BAD_DISPLAY         -> {
                throw RuntimeException(failedFunction + " failed - display is not an EGL display connection.")
            }
            EGL_NOT_INITIALIZED     -> {
                throw RuntimeException(failedFunction + " failed - display has not been initialized.")
            }
            EGL_BAD_SURFACE         -> {
                throw RuntimeException(failedFunction + " failed - draw or read is not an EGL surface.")
            }
            EGL_BAD_CONTEXT         -> {
                throw RuntimeException(failedFunction + " failed - context is not an EGL rendering context.")
            }
            EGL_BAD_MATCH           -> {
                throw RuntimeException(failedFunction + " failed - draw or read are not compatible with context, or " + "context is set to EGL_NO_CONTEXT and draw or read are not set to " + "EGL_NO_SURFACE, or draw or read are set to EGL_NO_SURFACE and context is " + "not set to EGL_NO_CONTEXT.")
            }
            EGL_BAD_ACCESS          -> {
                throw RuntimeException(failedFunction + " failed - context is current to some other thread.")
            }
            EGL_BAD_NATIVE_PIXMAP   -> {
                throw RuntimeException(failedFunction + " failed - a native pixmap underlying either draw or read is " + "no longer valid.")
            }
            EGL_BAD_NATIVE_WINDOW   -> {
                throw RuntimeException(failedFunction + " failed - a native window underlying either draw or read is " + "no longer valid.")
            }
            EGL_BAD_CURRENT_SURFACE -> {
                throw RuntimeException(failedFunction + " failed - the previous context has unflushed commands and " + "the previous surface is no longer valid.")
            }
            EGL_BAD_ALLOC           -> {
                throw RuntimeException(failedFunction + " failed - allocation of ancillary buffers for draw or read " + "were delayed until eglMakeCurrent is called, and there are not enough " + "resources to allocate them.")
            }
            EGL_CONTEXT_LOST        -> {
                throw RuntimeException(failedFunction + " failed - a power management event has occurred. The " + "application must destroy all contexts and reinitialise OpenGL ES state and " + "objects to continue rendering.")
            }
            EGL_BAD_ATTRIBUTE       -> {
                throw RuntimeException(failedFunction + " failed - Bad attribute")
            }
            EGL_BAD_CONFIG          -> {
                throw RuntimeException(failedFunction + " failed - Bad config")
            }
        }
    }

    external fun eglGetError(): Int

    companion object {

        val EGL_WAYLAND_BUFFER_WL = 0x31D5
        val EGL_WAYLAND_PLANE_WL = 0x31D6

        val EGL_TEXTURE_Y_U_V_WL = 0x31D7
        val EGL_TEXTURE_Y_UV_WL = 0x31D8
        val EGL_TEXTURE_Y_XUXV_WL = 0x31D9
        val EGL_TEXTURE_EXTERNAL_WL = 0x31DA

        val EGL_WAYLAND_Y_INVERTED_WL = 0x31DB

        val EGL_PLATFORM_WAYLAND_KHR = 0x31D8
        val EGL_PLATFORM_GBM_KHR = 0x31D7
        val EGL_PLATFORM_X11_KHR = 0x31D5
        val EGL_PLATFORM_X11_SCREEN_KHR = 0x31D6

        val EGL_ALPHA_SIZE = 0x3021
        val EGL_BAD_ACCESS = 0x3002
        val EGL_BAD_ALLOC = 0x3003
        val EGL_BAD_ATTRIBUTE = 0x3004
        val EGL_BAD_CONFIG = 0x3005
        val EGL_BAD_CONTEXT = 0x3006
        val EGL_BAD_CURRENT_SURFACE = 0x3007
        val EGL_BAD_DISPLAY = 0x3008
        val EGL_BAD_MATCH = 0x3009
        val EGL_BAD_NATIVE_PIXMAP = 0x300A
        val EGL_BAD_NATIVE_WINDOW = 0x300B
        val EGL_BAD_PARAMETER = 0x300C
        val EGL_BAD_SURFACE = 0x300D
        val EGL_BLUE_SIZE = 0x3022
        val EGL_BUFFER_SIZE = 0x3020
        val EGL_CONFIG_CAVEAT = 0x3027
        val EGL_CONFIG_ID = 0x3028
        val EGL_CORE_NATIVE_ENGINE = 0x305B
        val EGL_DEPTH_SIZE = 0x3025
        val EGL_DONT_CARE = -1
        val EGL_DRAW = 0x3059
        val EGL_EXTENSIONS = 0x3055
        val EGL_FALSE = 0
        val EGL_GREEN_SIZE = 0x3023
        val EGL_HEIGHT = 0x3056
        val EGL_LARGEST_PBUFFER = 0x3058
        val EGL_LEVEL = 0x3029
        val EGL_MAX_PBUFFER_HEIGHT = 0x302A
        val EGL_MAX_PBUFFER_PIXELS = 0x302B
        val EGL_MAX_PBUFFER_WIDTH = 0x302C
        val EGL_NATIVE_RENDERABLE = 0x302D
        val EGL_NATIVE_VISUAL_ID = 0x302E
        val EGL_NATIVE_VISUAL_TYPE = 0x302F
        val EGL_NONE = 0x3038
        val EGL_NON_CONFORMANT_CONFIG = 0x3051
        val EGL_NOT_INITIALIZED = 0x3001
        val EGL_NO_CONTEXT = 0L
        val EGL_NO_DISPLAY = 0L
        val EGL_NO_SURFACE = 0L
        val EGL_PBUFFER_BIT = 0x0001
        val EGL_PIXMAP_BIT = 0x0002
        val EGL_READ = 0x305A
        val EGL_RED_SIZE = 0x3024
        val EGL_SAMPLES = 0x3031
        val EGL_SAMPLE_BUFFERS = 0x3032
        val EGL_SLOW_CONFIG = 0x3050
        val EGL_STENCIL_SIZE = 0x3026
        val EGL_SUCCESS = 0x3000
        val EGL_SURFACE_TYPE = 0x3033
        val EGL_TRANSPARENT_BLUE_VALUE = 0x3035
        val EGL_TRANSPARENT_GREEN_VALUE = 0x3036
        val EGL_TRANSPARENT_RED_VALUE = 0x3037
        val EGL_TRANSPARENT_RGB = 0x3052
        val EGL_TRANSPARENT_TYPE = 0x3034
        val EGL_TRUE = 1
        val EGL_VENDOR = 0x3053
        val EGL_VERSION = 0x3054
        val EGL_WIDTH = 0x3057
        val EGL_WINDOW_BIT = 0x0004

        val EGL_BACK_BUFFER = 0x3084
        val EGL_BIND_TO_TEXTURE_RGB = 0x3039
        val EGL_BIND_TO_TEXTURE_RGBA = 0x303A
        val EGL_CONTEXT_LOST = 0x300E
        val EGL_MIN_SWAP_INTERVAL = 0x303B
        val EGL_MAX_SWAP_INTERVAL = 0x303C
        val EGL_MIPMAP_TEXTURE = 0x3082
        val EGL_MIPMAP_LEVEL = 0x3083
        val EGL_NO_TEXTURE = 0x305C
        val EGL_TEXTURE_2D = 0x305F
        val EGL_TEXTURE_FORMAT = 0x3080
        val EGL_TEXTURE_RGB = 0x305D
        val EGL_TEXTURE_RGBA = 0x305E
        val EGL_TEXTURE_TARGET = 0x3081

        val EGL_ALPHA_FORMAT = 0x3088
        val EGL_ALPHA_FORMAT_NONPRE = 0x308B
        val EGL_ALPHA_FORMAT_PRE = 0x308C
        val EGL_ALPHA_MASK_SIZE = 0x303E
        val EGL_BUFFER_PRESERVED = 0x3094
        val EGL_BUFFER_DESTROYED = 0x3095
        val EGL_CLIENT_APIS = 0x308D
        val EGL_COLORSPACE = 0x3087
        val EGL_COLORSPACE_sRGB = 0x3089
        val EGL_COLORSPACE_LINEAR = 0x308A
        val EGL_COLOR_BUFFER_TYPE = 0x303F
        val EGL_CONTEXT_CLIENT_TYPE = 0x3097
        val EGL_DISPLAY_SCALING = 10000
        val EGL_HORIZONTAL_RESOLUTION = 0x3090
        val EGL_LUMINANCE_BUFFER = 0x308F
        val EGL_LUMINANCE_SIZE = 0x303D
        val EGL_OPENGL_ES_BIT = 0x0001
        val EGL_OPENVG_BIT = 0x0002
        val EGL_OPENGL_ES_API = 0x30A0
        val EGL_OPENVG_API = 0x30A1
        val EGL_OPENVG_IMAGE = 0x3096
        val EGL_PIXEL_ASPECT_RATIO = 0x3092
        val EGL_RENDERABLE_TYPE = 0x3040
        val EGL_RENDER_BUFFER = 0x3086
        val EGL_RGB_BUFFER = 0x308E
        val EGL_SINGLE_BUFFER = 0x3085
        val EGL_SWAP_BEHAVIOR = 0x3093
        val EGL_UNKNOWN = -1
        val EGL_VERTICAL_RESOLUTION = 0x3091

        val EGL_CONFORMANT = 0x3042
        val EGL_CONTEXT_CLIENT_VERSION = 0x3098
        val EGL_MATCH_NATIVE_PIXMAP = 0x3041
        val EGL_OPENGL_ES2_BIT = 0x0004
        val EGL_VG_ALPHA_FORMAT = 0x3088
        val EGL_VG_ALPHA_FORMAT_NONPRE = 0x308B
        val EGL_VG_ALPHA_FORMAT_PRE = 0x308C
        val EGL_VG_ALPHA_FORMAT_PRE_BIT = 0x0040
        val EGL_VG_COLORSPACE = 0x3087
        val EGL_VG_COLORSPACE_sRGB = 0x3089
        val EGL_VG_COLORSPACE_LINEAR = 0x308A
        val EGL_VG_COLORSPACE_LINEAR_BIT = 0x0020

        val EGL_DEFAULT_DISPLAY = 0L
        val EGL_MULTISAMPLE_RESOLVE_BOX_BIT = 0x0200
        val EGL_MULTISAMPLE_RESOLVE = 0x3099
        val EGL_MULTISAMPLE_RESOLVE_DEFAULT = 0x309A
        val EGL_MULTISAMPLE_RESOLVE_BOX = 0x309B
        val EGL_OPENGL_API = 0x30A2
        val EGL_OPENGL_BIT = 0x0008
        val EGL_SWAP_BEHAVIOR_PRESERVED_BIT = 0x0400

        val EGL_CONTEXT_MAJOR_VERSION = 0x3098
        val EGL_CONTEXT_MINOR_VERSION = 0x30FB
        val EGL_CONTEXT_OPENGL_PROFILE_MASK = 0x30FD
        val EGL_CONTEXT_OPENGL_RESET_NOTIFICATION_STRATEGY = 0x31BD
        val EGL_NO_RESET_NOTIFICATION = 0x31BE
        val EGL_LOSE_CONTEXT_ON_RESET = 0x31BF
        val EGL_CONTEXT_OPENGL_CORE_PROFILE_BIT = 0x00000001
        val EGL_CONTEXT_OPENGL_COMPATIBILITY_PROFILE_BIT = 0x00000002
        val EGL_CONTEXT_OPENGL_DEBUG = 0x31B0
        val EGL_CONTEXT_OPENGL_FORWARD_COMPATIBLE = 0x31B1
        val EGL_CONTEXT_OPENGL_ROBUST_ACCESS = 0x31B2
        val EGL_OPENGL_ES3_BIT = 0x00000040
        val EGL_CL_EVENT_HANDLE = 0x309C
        val EGL_SYNC_CL_EVENT = 0x30FE
        val EGL_SYNC_CL_EVENT_COMPLETE = 0x30FF
        val EGL_SYNC_PRIOR_COMMANDS_COMPLETE = 0x30F0
        val EGL_SYNC_TYPE = 0x30F7
        val EGL_SYNC_STATUS = 0x30F1
        val EGL_SYNC_CONDITION = 0x30F8
        val EGL_SIGNALED = 0x30F2
        val EGL_UNSIGNALED = 0x30F3
        val EGL_SYNC_FLUSH_COMMANDS_BIT = 0x0001
        val EGL_TIMEOUT_EXPIRED = 0x30F5
        val EGL_CONDITION_SATISFIED = 0x30F6
        val EGL_NO_SYNC = 0L
        val EGL_SYNC_FENCE = 0x30F9
        val EGL_GL_COLORSPACE = 0x309D
        val EGL_GL_COLORSPACE_SRGB = 0x3089
        val EGL_GL_COLORSPACE_LINEAR = 0x308A
        val EGL_GL_RENDERBUFFER = 0x30B9
        val EGL_GL_TEXTURE_2D = 0x30B1
        val EGL_GL_TEXTURE_LEVEL = 0x30BC
        val EGL_GL_TEXTURE_3D = 0x30B2
        val EGL_GL_TEXTURE_ZOFFSET = 0x30BD
        val EGL_GL_TEXTURE_CUBE_MAP_POSITIVE_X = 0x30B3
        val EGL_GL_TEXTURE_CUBE_MAP_NEGATIVE_X = 0x30B4
        val EGL_GL_TEXTURE_CUBE_MAP_POSITIVE_Y = 0x30B5
        val EGL_GL_TEXTURE_CUBE_MAP_NEGATIVE_Y = 0x30B6
        val EGL_GL_TEXTURE_CUBE_MAP_POSITIVE_Z = 0x30B7
        val EGL_GL_TEXTURE_CUBE_MAP_NEGATIVE_Z = 0x30B8
        val EGL_IMAGE_PRESERVED = 0x30D2
        val EGL_NO_IMAGE = 0L
        val EGL_NO_IMAGE_KHR = 0L
    }
}
