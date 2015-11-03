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
package org.westmalle.wayland.nativ.libEGL;

import com.sun.jna.Function;
import com.sun.jna.LastErrorException;
import com.sun.jna.Pointer;
import org.westmalle.wayland.nativ.NativeString;

import java.util.Optional;

public class LibEGL {

    public static final Pointer EGL_DEFAULT_DISPLAY = null;
    public static final Pointer EGL_NO_CONTEXT      = null;
    public static final Pointer EGL_NO_DISPLAY      = null;
    public static final Pointer EGL_NO_SURFACE      = null;

    public static final int EGL_PLATFORM_ANDROID_KHR    = 0x3141;
    public static final int EGL_PLATFORM_GBM_KHR        = 0x31D7;
    public static final int EGL_PLATFORM_WAYLAND_KHR    = 0x31D8;
    public static final int EGL_PLATFORM_X11_KHR        = 0x31D5;
    public static final int EGL_PLATFORM_X11_SCREEN_KHR = 0x31D6;
    public static final int EGL_PLATFORM_GBM_MESA       = 0x31D7;
    public static final int EGL_OPENGL_ES_API           = 0x30A0;
    public static final int EGL_OPENGL_API              = 0x30A2;
    public static final int EGL_WAYLAND_BUFFER_WL       = 0x31D5;
    public static final int EGL_WAYLAND_PLANE_WL        = 0x31D6;
    public static final int EGL_TEXTURE_Y_U_V_WL        = 0x31D7;
    public static final int EGL_TEXTURE_Y_UV_WL         = 0x31D8;
    public static final int EGL_TEXTURE_Y_XUXV_WL       = 0x31D9;
    public static final int EGL_WAYLAND_Y_INVERTED_WL   = 0x31DB;
    public static final int EGL_COLOR_BUFFER_TYPE       = 0x303F;
    public static final int EGL_RGB_BUFFER              = 0x308E;
    public static final int EGL_BUFFER_SIZE             = 0x3020;
    public static final int EGL_RED_SIZE                = 0x3024;
    public static final int EGL_GREEN_SIZE              = 0x3023;
    public static final int EGL_BLUE_SIZE               = 0x3022;
    public static final int EGL_ALPHA_SIZE              = 0x3021;
    public static final int EGL_DEPTH_SIZE              = 0x3025;
    public static final int EGL_STENCIL_SIZE            = 0x3026;
    public static final int EGL_SAMPLE_BUFFERS          = 0x3032;
    public static final int EGL_SAMPLES                 = 0x3031;
    public static final int EGL_SURFACE_TYPE            = 0x3033;
    public static final int EGL_RENDERABLE_TYPE         = 0x3040;
    public static final int EGL_OPENGL_ES2_BIT          = 0x0004;
    public static final int EGL_NONE                    = 0x3038;
    public static final int EGL_CONTEXT_CLIENT_VERSION  = 0x3098;
    public static final int EGL_BACK_BUFFER             = 0x3084;
    public static final int EGL_SINGLE_BUFFER           = 0x3085;
    public static final int EGL_VENDOR                  = 0x3053;
    public static final int EGL_VERSION                 = 0x3054;
    public static final int EGL_EXTENSIONS              = 0x3055;
    public static final int EGL_CLIENT_APIS             = 0x308D;

    public static final int EGL_SUCCESS             = 0x3000;
    public static final int EGL_NOT_INITIALIZED     = 0x3001;
    public static final int EGL_BAD_ACCESS          = 0x3002;
    public static final int EGL_BAD_ALLOC           = 0x3003;
    public static final int EGL_BAD_ATTRIBUTE       = 0x3004;
    public static final int EGL_BAD_CONFIG          = 0x3005;
    public static final int EGL_BAD_CONTEXT         = 0x3006;
    public static final int EGL_BAD_CURRENT_SURFACE = 0x3007;
    public static final int EGL_BAD_DISPLAY         = 0x3008;
    public static final int EGL_BAD_MATCH           = 0x3009;
    public static final int EGL_BAD_NATIVE_PIXMAP   = 0x300A;
    public static final int EGL_BAD_NATIVE_WINDOW   = 0x300B;
    public static final int EGL_BAD_PARAMETER       = 0x300C;
    public static final int EGL_BAD_SURFACE         = 0x300D;
    public static final int EGL_CONTEXT_LOST        = 0x300E;

    public static final int EGL_PBUFFER_BIT                 = 0x0001	/* EGL_SURFACE_TYPE mask bits */;
    public static final int EGL_PIXMAP_BIT                  = 0x0002	/* EGL_SURFACE_TYPE mask bits */;
    public static final int EGL_WINDOW_BIT                  = 0x0004	/* EGL_SURFACE_TYPE mask bits */;
    public static final int EGL_VG_COLORSPACE_LINEAR_BIT    = 0x0020	/* EGL_SURFACE_TYPE mask bits */;
    public static final int EGL_VG_ALPHA_FORMAT_PRE_BIT     = 0x0040	/* EGL_SURFACE_TYPE mask bits */;
    public static final int EGL_MULTISAMPLE_RESOLVE_BOX_BIT = 0x0200	/* EGL_SURFACE_TYPE mask bits */;
    public static final int EGL_SWAP_BEHAVIOR_PRESERVED_BIT = 0x0400	/* EGL_SURFACE_TYPE mask bits */;

    /* QuerySurface / CreatePbufferSurface targets */
    public static final int EGL_HEIGHT                = 0x3056;
    public static final int EGL_WIDTH                 = 0x3057;
    public static final int EGL_LARGEST_PBUFFER       = 0x3058;
    public static final int EGL_TEXTURE_FORMAT        = 0x3080;
    public static final int EGL_TEXTURE_TARGET        = 0x3081;
    public static final int EGL_MIPMAP_TEXTURE        = 0x3082;
    public static final int EGL_MIPMAP_LEVEL          = 0x3083;
    public static final int EGL_RENDER_BUFFER         = 0x3086;
    public static final int EGL_COLORSPACE            = 0x3087;
    public static final int EGL_ALPHA_FORMAT          = 0x3088;
    public static final int EGL_HORIZONTAL_RESOLUTION = 0x3090;
    public static final int EGL_VERTICAL_RESOLUTION   = 0x3091;
    public static final int EGL_PIXEL_ASPECT_RATIO    = 0x3092;
    public static final int EGL_SWAP_BEHAVIOR         = 0x3093;

    /* Back buffer swap behaviors */
    public static final int EGL_BUFFER_PRESERVED = 0x3094	/* EGL_SWAP_BEHAVIOR value */;
    public static final int EGL_BUFFER_DESTROYED = 0x3095	/* EGL_SWAP_BEHAVIOR value */;


    private Optional<Function> eglCreatePlatformWindowSurfaceEXT = Optional.empty();
    private Optional<Function> eglGetPlatformDisplayEXT          = Optional.empty();

//    public boolean eglBindWaylandDisplayWL(Pointer dpy,
//                                           Pointer display) {
//        final Function function = getFunction("eglBindWaylandDisplayWL");
//        return (boolean) function.invoke(boolean.class,
//                                         new Object[]{
//                                                 dpy,
//                                                 display
//                                         });
//    }

//    public boolean eglUnbindWaylandDisplayWL(Pointer dpy,
//                                             Pointer display) {
//        final Function function = getFunction("eglUnbindWaylandDisplayWL");
//        return (boolean) function.invoke(boolean.class, new Object[]{
//                dpy,
//                display
//        });
//    }

//    public boolean eglQueryWaylandBufferWL(Pointer dpy,
//                                           Pointer buffer,
//                                           int attribute,
//                                           Pointer value) {
//        final Function function = getFunction("eglQueryWaylandBufferWL");
//        return (boolean) function.invoke(boolean.class, new Object[]{
//                dpy,
//                buffer,
//                attribute,
//                value
//        });
//    }

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

    public native Pointer eglGetDisplay(Pointer native_display);

    public native Pointer eglCreateWindowSurface(Pointer display,
                                                 Pointer config,
                                                 Pointer native_window,
                                                 Pointer attrib_list);

    public Pointer eglGetPlatformDisplayEXT(final int platform,
                                            final Pointer native_display,
                                            final Pointer attrib_list) {
        return (Pointer) this.eglGetPlatformDisplayEXT.orElseThrow(UnsupportedOperationException::new)
                                                      .invoke(Pointer.class,
                                                              new Object[]{
                                                                      platform,
                                                                      native_display,
                                                                      attrib_list
                                                              });
    }

    public native Pointer eglQueryString(Pointer dpy,
                                         int name) throws LastErrorException;

    public Pointer eglCreatePlatformWindowSurfaceEXT(final Pointer dpy,
                                                     final Pointer config,
                                                     final Pointer native_window,
                                                     final Pointer attrib_list) {
        return (Pointer) this.eglCreatePlatformWindowSurfaceEXT.orElseThrow(UnsupportedOperationException::new)
                                                               .invoke(
                                                                       Pointer.class,
                                                                       new Object[]{
                                                                               dpy,
                                                                               config,
                                                                               native_window,
                                                                               attrib_list
                                                                       });
    }

    public native boolean eglChooseConfig(Pointer dpy,
                                          Pointer attrib_list,
                                          Pointer configs,
                                          int config_size,
                                          Pointer num_config);

    public native boolean eglQueryContext(Pointer dpy,
                                          Pointer ctx,
                                          int attribute,
                                          Pointer value);

    public void loadEglCreatePlatformWindowSurfaceEXT() {
        this.eglCreatePlatformWindowSurfaceEXT = Optional.of(loadFunction("eglCreatePlatformWindowSurfaceEXT"));
    }

    private Function loadFunction(final String name) {
        final NativeString procname        = new NativeString(name);
        final Pointer      functionPointer = eglGetProcAddress(procname.getPointer());
        return Function.getFunction(functionPointer);
    }

    public native Pointer eglGetProcAddress(Pointer procname);

    public void loadEglGetPlatformDisplayEXT() {
        this.eglGetPlatformDisplayEXT = Optional.of(loadFunction("eglGetPlatformDisplayEXT"));
    }

    public native int eglGetError();

    public native int eglSurfaceAttrib(Pointer display,
                                       Pointer surface,
                                       int attribute,
                                       int value);

    public void throwError(final String failedFunction) throws RuntimeException {
        //looks like there was an error trying to make the egl context current.
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
