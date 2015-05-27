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

public class Libegl {

    static {
        Native.register("egl");
    }

    Libegl() {
    }

    public final int EGL_PLATFORM_ANDROID_KHR    = 0x3141;
    public final int EGL_PLATFORM_GBM_KHR        = 0x31D7;
    public final int EGL_PLATFORM_WAYLAND_KHR    = 0x31D8;
    public final int EGL_PLATFORM_X11_KHR        = 0x31D5;
    public final int EGL_PLATFORM_X11_SCREEN_KHR = 0x31D6;
    public final int EGL_PLATFORM_GBM_MESA       = 0x31D7;

    public final int EGL_WAYLAND_BUFFER_WL     = 0x31D5;
    public final int EGL_WAYLAND_PLANE_WL      = 0x31D6;
    public final int EGL_TEXTURE_Y_U_V_WL      = 0x31D7;
    public final int EGL_TEXTURE_Y_UV_WL       = 0x31D8;
    public final int EGL_TEXTURE_Y_XUXV_WL     = 0x31D9;
    public final int EGL_TEXTURE_FORMAT        = 0x3080;
    public final int EGL_WAYLAND_Y_INVERTED_WL = 0x31DB;

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
}
