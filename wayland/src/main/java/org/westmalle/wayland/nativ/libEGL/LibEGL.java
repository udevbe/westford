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
package org.westmalle.wayland.nativ.libEGL;

import org.freedesktop.jaccall.Lib;
import org.freedesktop.jaccall.Ptr;

import javax.inject.Singleton;

@Singleton
@Lib(value = "EGL",
     version = 1)
public class LibEGL {

    public static final int EGL_WAYLAND_BUFFER_WL = 0x31D5;
    public static final int EGL_WAYLAND_PLANE_WL  = 0x31D6;

    public static final int EGL_TEXTURE_Y_U_V_WL    = 0x31D7;
    public static final int EGL_TEXTURE_Y_UV_WL     = 0x31D8;
    public static final int EGL_TEXTURE_Y_XUXV_WL   = 0x31D9;
    public static final int EGL_TEXTURE_EXTERNAL_WL = 0x31DA;


    public static final int EGL_WAYLAND_Y_INVERTED_WL = 0x31DB;

    public static final int EGL_PLATFORM_WAYLAND_KHR    = 0x31D8;
    public static final int EGL_PLATFORM_GBM_KHR        = 0x31D7;
    public static final int EGL_PLATFORM_X11_KHR        = 0x31D5;
    public static final int EGL_PLATFORM_X11_SCREEN_KHR = 0x31D6;

    public static final int  EGL_ALPHA_SIZE              = 0x3021;
    public static final int  EGL_BAD_ACCESS              = 0x3002;
    public static final int  EGL_BAD_ALLOC               = 0x3003;
    public static final int  EGL_BAD_ATTRIBUTE           = 0x3004;
    public static final int  EGL_BAD_CONFIG              = 0x3005;
    public static final int  EGL_BAD_CONTEXT             = 0x3006;
    public static final int  EGL_BAD_CURRENT_SURFACE     = 0x3007;
    public static final int  EGL_BAD_DISPLAY             = 0x3008;
    public static final int  EGL_BAD_MATCH               = 0x3009;
    public static final int  EGL_BAD_NATIVE_PIXMAP       = 0x300A;
    public static final int  EGL_BAD_NATIVE_WINDOW       = 0x300B;
    public static final int  EGL_BAD_PARAMETER           = 0x300C;
    public static final int  EGL_BAD_SURFACE             = 0x300D;
    public static final int  EGL_BLUE_SIZE               = 0x3022;
    public static final int  EGL_BUFFER_SIZE             = 0x3020;
    public static final int  EGL_CONFIG_CAVEAT           = 0x3027;
    public static final int  EGL_CONFIG_ID               = 0x3028;
    public static final int  EGL_CORE_NATIVE_ENGINE      = 0x305B;
    public static final int  EGL_DEPTH_SIZE              = 0x3025;
    public static final int  EGL_DONT_CARE               = -1;
    public static final int  EGL_DRAW                    = 0x3059;
    public static final int  EGL_EXTENSIONS              = 0x3055;
    public static final int  EGL_FALSE                   = 0;
    public static final int  EGL_GREEN_SIZE              = 0x3023;
    public static final int  EGL_HEIGHT                  = 0x3056;
    public static final int  EGL_LARGEST_PBUFFER         = 0x3058;
    public static final int  EGL_LEVEL                   = 0x3029;
    public static final int  EGL_MAX_PBUFFER_HEIGHT      = 0x302A;
    public static final int  EGL_MAX_PBUFFER_PIXELS      = 0x302B;
    public static final int  EGL_MAX_PBUFFER_WIDTH       = 0x302C;
    public static final int  EGL_NATIVE_RENDERABLE       = 0x302D;
    public static final int  EGL_NATIVE_VISUAL_ID        = 0x302E;
    public static final int  EGL_NATIVE_VISUAL_TYPE      = 0x302F;
    public static final int  EGL_NONE                    = 0x3038;
    public static final int  EGL_NON_CONFORMANT_CONFIG   = 0x3051;
    public static final int  EGL_NOT_INITIALIZED         = 0x3001;
    public static final long EGL_NO_CONTEXT              = 0L;
    public static final long EGL_NO_DISPLAY              = 0L;
    public static final long EGL_NO_SURFACE              = 0L;
    public static final int  EGL_PBUFFER_BIT             = 0x0001;
    public static final int  EGL_PIXMAP_BIT              = 0x0002;
    public static final int  EGL_READ                    = 0x305A;
    public static final int  EGL_RED_SIZE                = 0x3024;
    public static final int  EGL_SAMPLES                 = 0x3031;
    public static final int  EGL_SAMPLE_BUFFERS          = 0x3032;
    public static final int  EGL_SLOW_CONFIG             = 0x3050;
    public static final int  EGL_STENCIL_SIZE            = 0x3026;
    public static final int  EGL_SUCCESS                 = 0x3000;
    public static final int  EGL_SURFACE_TYPE            = 0x3033;
    public static final int  EGL_TRANSPARENT_BLUE_VALUE  = 0x3035;
    public static final int  EGL_TRANSPARENT_GREEN_VALUE = 0x3036;
    public static final int  EGL_TRANSPARENT_RED_VALUE   = 0x3037;
    public static final int  EGL_TRANSPARENT_RGB         = 0x3052;
    public static final int  EGL_TRANSPARENT_TYPE        = 0x3034;
    public static final int  EGL_TRUE                    = 1;
    public static final int  EGL_VENDOR                  = 0x3053;
    public static final int  EGL_VERSION                 = 0x3054;
    public static final int  EGL_WIDTH                   = 0x3057;
    public static final int  EGL_WINDOW_BIT              = 0x0004;

    public static final int EGL_BACK_BUFFER          = 0x3084;
    public static final int EGL_BIND_TO_TEXTURE_RGB  = 0x3039;
    public static final int EGL_BIND_TO_TEXTURE_RGBA = 0x303A;
    public static final int EGL_CONTEXT_LOST         = 0x300E;
    public static final int EGL_MIN_SWAP_INTERVAL    = 0x303B;
    public static final int EGL_MAX_SWAP_INTERVAL    = 0x303C;
    public static final int EGL_MIPMAP_TEXTURE       = 0x3082;
    public static final int EGL_MIPMAP_LEVEL         = 0x3083;
    public static final int EGL_NO_TEXTURE           = 0x305C;
    public static final int EGL_TEXTURE_2D           = 0x305F;
    public static final int EGL_TEXTURE_FORMAT       = 0x3080;
    public static final int EGL_TEXTURE_RGB          = 0x305D;
    public static final int EGL_TEXTURE_RGBA         = 0x305E;
    public static final int EGL_TEXTURE_TARGET       = 0x3081;

    public static final int EGL_ALPHA_FORMAT          = 0x3088;
    public static final int EGL_ALPHA_FORMAT_NONPRE   = 0x308B;
    public static final int EGL_ALPHA_FORMAT_PRE      = 0x308C;
    public static final int EGL_ALPHA_MASK_SIZE       = 0x303E;
    public static final int EGL_BUFFER_PRESERVED      = 0x3094;
    public static final int EGL_BUFFER_DESTROYED      = 0x3095;
    public static final int EGL_CLIENT_APIS           = 0x308D;
    public static final int EGL_COLORSPACE            = 0x3087;
    public static final int EGL_COLORSPACE_sRGB       = 0x3089;
    public static final int EGL_COLORSPACE_LINEAR     = 0x308A;
    public static final int EGL_COLOR_BUFFER_TYPE     = 0x303F;
    public static final int EGL_CONTEXT_CLIENT_TYPE   = 0x3097;
    public static final int EGL_DISPLAY_SCALING       = 10000;
    public static final int EGL_HORIZONTAL_RESOLUTION = 0x3090;
    public static final int EGL_LUMINANCE_BUFFER      = 0x308F;
    public static final int EGL_LUMINANCE_SIZE        = 0x303D;
    public static final int EGL_OPENGL_ES_BIT         = 0x0001;
    public static final int EGL_OPENVG_BIT            = 0x0002;
    public static final int EGL_OPENGL_ES_API         = 0x30A0;
    public static final int EGL_OPENVG_API            = 0x30A1;
    public static final int EGL_OPENVG_IMAGE          = 0x3096;
    public static final int EGL_PIXEL_ASPECT_RATIO    = 0x3092;
    public static final int EGL_RENDERABLE_TYPE       = 0x3040;
    public static final int EGL_RENDER_BUFFER         = 0x3086;
    public static final int EGL_RGB_BUFFER            = 0x308E;
    public static final int EGL_SINGLE_BUFFER         = 0x3085;
    public static final int EGL_SWAP_BEHAVIOR         = 0x3093;
    public static final int EGL_UNKNOWN               = -1;
    public static final int EGL_VERTICAL_RESOLUTION   = 0x3091;

    public static final int EGL_CONFORMANT              = 0x3042;
    public static final int EGL_CONTEXT_CLIENT_VERSION  = 0x3098;
    public static final int EGL_MATCH_NATIVE_PIXMAP     = 0x3041;
    public static final int EGL_OPENGL_ES2_BIT          = 0x0004;
    public static final int EGL_VG_ALPHA_FORMAT         = 0x3088;
    public static final int EGL_VG_ALPHA_FORMAT_NONPRE  = 0x308B;
    public static final int EGL_VG_ALPHA_FORMAT_PRE     = 0x308C;
    public static final int EGL_VG_ALPHA_FORMAT_PRE_BIT = 0x0040;
    ;
    public static final int EGL_VG_COLORSPACE            = 0x3087;
    public static final int EGL_VG_COLORSPACE_sRGB       = 0x3089;
    public static final int EGL_VG_COLORSPACE_LINEAR     = 0x308A;
    public static final int EGL_VG_COLORSPACE_LINEAR_BIT = 0x0020;

    public static final long EGL_DEFAULT_DISPLAY             = 0L;
    public static final int  EGL_MULTISAMPLE_RESOLVE_BOX_BIT = 0x0200;
    public static final int  EGL_MULTISAMPLE_RESOLVE         = 0x3099;
    public static final int  EGL_MULTISAMPLE_RESOLVE_DEFAULT = 0x309A;
    public static final int  EGL_MULTISAMPLE_RESOLVE_BOX     = 0x309B;
    public static final int  EGL_OPENGL_API                  = 0x30A2;
    public static final int  EGL_OPENGL_BIT                  = 0x0008;
    public static final int  EGL_SWAP_BEHAVIOR_PRESERVED_BIT = 0x0400;

    public static final int  EGL_CONTEXT_MAJOR_VERSION                      = 0x3098;
    public static final int  EGL_CONTEXT_MINOR_VERSION                      = 0x30FB;
    public static final int  EGL_CONTEXT_OPENGL_PROFILE_MASK                = 0x30FD;
    public static final int  EGL_CONTEXT_OPENGL_RESET_NOTIFICATION_STRATEGY = 0x31BD;
    public static final int  EGL_NO_RESET_NOTIFICATION                      = 0x31BE;
    public static final int  EGL_LOSE_CONTEXT_ON_RESET                      = 0x31BF;
    public static final int  EGL_CONTEXT_OPENGL_CORE_PROFILE_BIT            = 0x00000001;
    public static final int  EGL_CONTEXT_OPENGL_COMPATIBILITY_PROFILE_BIT   = 0x00000002;
    public static final int  EGL_CONTEXT_OPENGL_DEBUG                       = 0x31B0;
    public static final int  EGL_CONTEXT_OPENGL_FORWARD_COMPATIBLE          = 0x31B1;
    public static final int  EGL_CONTEXT_OPENGL_ROBUST_ACCESS               = 0x31B2;
    public static final int  EGL_OPENGL_ES3_BIT                             = 0x00000040;
    public static final int  EGL_CL_EVENT_HANDLE                            = 0x309C;
    public static final int  EGL_SYNC_CL_EVENT                              = 0x30FE;
    public static final int  EGL_SYNC_CL_EVENT_COMPLETE                     = 0x30FF;
    public static final int  EGL_SYNC_PRIOR_COMMANDS_COMPLETE               = 0x30F0;
    public static final int  EGL_SYNC_TYPE                                  = 0x30F7;
    public static final int  EGL_SYNC_STATUS                                = 0x30F1;
    public static final int  EGL_SYNC_CONDITION                             = 0x30F8;
    public static final int  EGL_SIGNALED                                   = 0x30F2;
    public static final int  EGL_UNSIGNALED                                 = 0x30F3;
    public static final int  EGL_SYNC_FLUSH_COMMANDS_BIT                    = 0x0001;
    public static final long EGL_FOREVER                                    = 0xFFFFFFFFFFFFFFFFL;
    public static final int  EGL_TIMEOUT_EXPIRED                            = 0x30F5;
    public static final int  EGL_CONDITION_SATISFIED                        = 0x30F6;
    public static final long EGL_NO_SYNC                                    = 0L;
    public static final int  EGL_SYNC_FENCE                                 = 0x30F9;
    public static final int  EGL_GL_COLORSPACE                              = 0x309D;
    public static final int  EGL_GL_COLORSPACE_SRGB                         = 0x3089;
    public static final int  EGL_GL_COLORSPACE_LINEAR                       = 0x308A;
    public static final int  EGL_GL_RENDERBUFFER                            = 0x30B9;
    public static final int  EGL_GL_TEXTURE_2D                              = 0x30B1;
    public static final int  EGL_GL_TEXTURE_LEVEL                           = 0x30BC;
    public static final int  EGL_GL_TEXTURE_3D                              = 0x30B2;
    public static final int  EGL_GL_TEXTURE_ZOFFSET                         = 0x30BD;
    public static final int  EGL_GL_TEXTURE_CUBE_MAP_POSITIVE_X             = 0x30B3;
    public static final int  EGL_GL_TEXTURE_CUBE_MAP_NEGATIVE_X             = 0x30B4;
    public static final int  EGL_GL_TEXTURE_CUBE_MAP_POSITIVE_Y             = 0x30B5;
    public static final int  EGL_GL_TEXTURE_CUBE_MAP_NEGATIVE_Y             = 0x30B6;
    public static final int  EGL_GL_TEXTURE_CUBE_MAP_POSITIVE_Z             = 0x30B7;
    public static final int  EGL_GL_TEXTURE_CUBE_MAP_NEGATIVE_Z             = 0x30B8;
    public static final int  EGL_IMAGE_PRESERVED                            = 0x30D2;
    public static final long EGL_NO_IMAGE                                   = 0L;
    public static final long EGL_NO_IMAGE_KHR                               = 0L;

    public native int eglInitialize(@Ptr long dpy,
                                    @Ptr long major,
                                    @Ptr long minor);

    public native int eglTerminate(@Ptr long dpy);

    public native int eglBindAPI(int api);

    public native int eglSwapBuffers(@Ptr long dpy,
                                     @Ptr long surface);

    public native int eglMakeCurrent(@Ptr long dpy,
                                     @Ptr long draw,
                                     @Ptr long read,
                                     @Ptr long context);

    @Ptr
    public native long eglCreateContext(@Ptr long dpy,
                                        @Ptr long config,
                                        @Ptr long share_context,
                                        @Ptr long attrib_list);

    @Ptr
    public native long eglGetDisplay(@Ptr long native_display);

    @Ptr
    public native long eglCreateWindowSurface(@Ptr long display,
                                              @Ptr long config,
                                              @Ptr long native_window,
                                              @Ptr long attrib_list);

    @Ptr
    public native long eglQueryString(@Ptr long dpy,
                                      int name);

    public native int eglChooseConfig(@Ptr long dpy,
                                      @Ptr long attrib_list,
                                      @Ptr long configs,
                                      int config_size,
                                      @Ptr long num_config);

    public native int eglQueryContext(@Ptr long dpy,
                                      @Ptr long ctx,
                                      int attribute,
                                      @Ptr long value);

    @Ptr
    public native long eglGetProcAddress(@Ptr long procname);

    public native int eglGetError();

    public native int eglGetConfigs(@Ptr long display,
                                    @Ptr long configs,
                                    int config_size,
                                    @Ptr long num_config);

    public native int eglSurfaceAttrib(@Ptr long display,
                                       @Ptr long surface,
                                       int attribute,
                                       int value);

    public void throwError(final String failedFunction) throws RuntimeException {
        final int eglError = eglGetError();
        switch (eglError) {
            case EGL_SUCCESS: {
                System.err.println(failedFunction + " success.");
                break;
            }
            case EGL_BAD_DISPLAY: {
                throw new RuntimeException(failedFunction + " failed - display is not an EGL display connection.");
            }
            case EGL_NOT_INITIALIZED: {
                throw new RuntimeException(failedFunction + " failed - display has not been initialized.");
            }
            case EGL_BAD_SURFACE: {
                throw new RuntimeException(failedFunction + " failed - draw or read is not an EGL surface.");
            }
            case EGL_BAD_CONTEXT: {
                throw new RuntimeException(failedFunction + " failed - context is not an EGL rendering context.");
            }
            case EGL_BAD_MATCH: {
                throw new RuntimeException(failedFunction + " failed - draw or read are not compatible with context, or " +
                                           "context is set to EGL_NO_CONTEXT and draw or read are not set to " +
                                           "EGL_NO_SURFACE, or draw or read are set to EGL_NO_SURFACE and context is " +
                                           "not set to EGL_NO_CONTEXT.");
            }
            case EGL_BAD_ACCESS: {
                throw new RuntimeException(failedFunction + " failed - context is current to some other thread.");
            }
            case EGL_BAD_NATIVE_PIXMAP: {
                throw new RuntimeException(failedFunction + " failed - a native pixmap underlying either draw or read is " +
                                           "no longer valid.");
            }
            case EGL_BAD_NATIVE_WINDOW: {
                throw new RuntimeException(failedFunction + " failed - a native window underlying either draw or read is " +
                                           "no longer valid.");
            }
            case EGL_BAD_CURRENT_SURFACE: {
                throw new RuntimeException(failedFunction + " failed - the previous context has unflushed commands and " +
                                           "the previous surface is no longer valid.");
            }
            case EGL_BAD_ALLOC: {
                throw new RuntimeException(failedFunction + " failed - allocation of ancillary buffers for draw or read " +
                                           "were delayed until eglMakeCurrent is called, and there are not enough " +
                                           "resources to allocate them.");
            }
            case EGL_CONTEXT_LOST: {
                throw new RuntimeException(failedFunction + " failed - a power management event has occurred. The " +
                                           "application must destroy all contexts and reinitialise OpenGL ES state and " +
                                           "objects to continue rendering.");
            }
            case EGL_BAD_ATTRIBUTE: {
                throw new RuntimeException(failedFunction + " failed - Bad attribute");
            }
            case EGL_BAD_CONFIG: {
                throw new RuntimeException(failedFunction + " failed - Bad config");
            }
        }
    }
}
