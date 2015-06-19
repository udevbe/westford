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
package org.westmalle.wayland.nativ;

import com.sun.jna.Native;
import com.sun.jna.Pointer;

public class LibEGL {

    static {
        Native.register("EGL");
    }

    LibEGL() {
    }

    public static final Pointer EGL_NO_DISPLAY              = Pointer.createConstant(0);
    public static final int     EGL_PLATFORM_ANDROID_KHR    = 0x3141;
    public static final int     EGL_PLATFORM_GBM_KHR        = 0x31D7;
    public static final int     EGL_PLATFORM_WAYLAND_KHR    = 0x31D8;
    public static final int     EGL_PLATFORM_X11_KHR        = 0x31D5;
    public static final int     EGL_PLATFORM_X11_SCREEN_KHR = 0x31D6;
    public static final int     EGL_PLATFORM_GBM_MESA       = 0x31D7;
    public static final int     EGL_OPENGL_ES_API           = 0x30A0;
    public static final int     EGL_OPENGL_API              = 0x30A2;
    public static final int     EGL_WAYLAND_BUFFER_WL       = 0x31D5;
    public static final int     EGL_WAYLAND_PLANE_WL        = 0x31D6;
    public static final int     EGL_TEXTURE_Y_U_V_WL        = 0x31D7;
    public static final int     EGL_TEXTURE_Y_UV_WL         = 0x31D8;
    public static final int     EGL_TEXTURE_Y_XUXV_WL       = 0x31D9;
    public static final int     EGL_TEXTURE_FORMAT          = 0x3080;
    public static final int     EGL_WAYLAND_Y_INVERTED_WL   = 0x31DB;
    public static final int     EGL_COLOR_BUFFER_TYPE       = 0x303F;
    public static final int     EGL_RGB_BUFFER              = 0x308E;
    public static final int     EGL_BUFFER_SIZE             = 0x3020;
    public static final int     EGL_RED_SIZE                = 0x3024;
    public static final int     EGL_GREEN_SIZE              = 0x3023;
    public static final int     EGL_BLUE_SIZE               = 0x3022;
    public static final int     EGL_ALPHA_SIZE              = 0x3021;
    public static final int     EGL_DEPTH_SIZE              = 0x3025;
    public static final int     EGL_STENCIL_SIZE            = 0x3026;
    public static final int     EGL_SAMPLE_BUFFERS          = 0x3032;
    public static final int     EGL_SAMPLES                 = 0x3031;
    public static final int     EGL_SURFACE_TYPE            = 0x3033;
    public static final int     EGL_WINDOW_BIT              = 0x0004;
    public static final int     EGL_RENDERABLE_TYPE         = 0x3040;
    public static final int     EGL_OPENGL_ES2_BIT          = 0x0004;
    public static final int     EGL_NONE                    = 0x3038;
    public static final int     EGL_CONTEXT_CLIENT_VERSION  = 0x3098;
    public static final Pointer EGL_NO_CONTEXT              = Pointer.createConstant(0);
    public static final int     EGL_BACK_BUFFER             = 0x3084;
    public static final int     EGL_RENDER_BUFFER           = 0x3086;
    public static final int     EGL_SINGLE_BUFFER           = 0x3085;


    public native boolean eglBindWaylandDisplayWL(Pointer dpy,
                                                  Pointer display);

    public native boolean eglUnbindWaylandDisplayWL(Pointer dpy,
                                                    Pointer display);

    public native boolean eglQueryWaylandBufferWL(Pointer dpy,
                                                  Pointer buffer,
                                                  int attribute,
                                                  Pointer value);

    public native boolean eglInitialize(Pointer dpy,
                                        Pointer major,
                                        Pointer minor);

    public native boolean eglTerminate(Pointer dpy);

    public native boolean eglBindAPI(int api);

    public native boolean eglSwapBuffers(Pointer dpy,
                                         Pointer surface);

    public native boolean eglMakeCurrent(Pointer dpy,
                                         Pointer draw,
                                         Pointer read,
                                         Pointer context);

    public native Pointer eglCreateContext(Pointer dpy,
                                           Pointer config,
                                           Pointer share_context,
                                           Pointer attrib_list);

    public native Pointer eglGetPlatformDisplayEXT(int platform,
                                                   Pointer native_display,
                                                   Pointer attrib_list);

    public native Pointer eglCreatePlatformWindowSurfaceEXT(Pointer dpy,
                                                            Pointer config,
                                                            Pointer native_window,
                                                            Pointer attrib_list);

    public native boolean eglChooseConfig(Pointer dpy,
                                          Pointer attrib_list,
                                          Pointer configs,
                                          int config_size,
                                          Pointer num_config);

    public native boolean eglQueryContext(Pointer dpy,
                                          Pointer ctx,
                                          int attribute,
                                          Pointer value);


}
