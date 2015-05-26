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
