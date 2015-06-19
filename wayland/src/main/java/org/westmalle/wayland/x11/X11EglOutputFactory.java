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
package org.westmalle.wayland.x11;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import org.westmalle.wayland.nativ.LibEGL;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import static org.westmalle.wayland.nativ.LibEGL.*;

public class X11EglOutputFactory {

    @Nonnull
    private final LibEGL libEGL;

    @Inject
    X11EglOutputFactory(@Nonnull final LibEGL libEGL) {
        this.libEGL = libEGL;
    }

    @Nonnull
    public X11EglOutput create(@Nonnull final Pointer display,
                               final int window) {
        if (!this.libEGL.eglBindAPI(EGL_OPENGL_ES_API)) {
            throw new RuntimeException("eglBindAPI failed");
        }
        final Pointer eglDisplay   = createEglDisplay(display);
        final int     configs_size = 256 * Pointer.SIZE;
        final Pointer configs      = new Memory(configs_size);
        chooseConfig(eglDisplay,
                     configs,
                     configs_size);
        final Pointer config = configs.getPointer(0);
        final Pointer context = createEglContext(eglDisplay,
                                                 config);
        return new X11EglOutput(this.libEGL,
                                eglDisplay,
                                createEglSurface(eglDisplay,
                                                 config,
                                                 context,
                                                 window),
                                context);
    }

    private Pointer createEglDisplay(final Pointer nativeDisplay) {

        final Pointer eglDisplay = this.libEGL.eglGetPlatformDisplayEXT(EGL_PLATFORM_X11_KHR,
                                                                        nativeDisplay,
                                                                        null);
        if (eglDisplay == null || eglDisplay.equals(EGL_NO_DISPLAY)) {
            throw new RuntimeException("eglGetDisplay() failed");
        }
        if (!this.libEGL.eglInitialize(eglDisplay,
                                       null,
                                       null)) {
            throw new RuntimeException("eglInitialize() failed");
        }
        return eglDisplay;
    }

    private void chooseConfig(final Pointer eglDisplay,
                              final Pointer configs,
                              final int configs_size) {
        final Pointer num_configs        = new Memory(Integer.BYTES);
        final Pointer egl_config_attribs = createEglConfigAttribs();
        if (!this.libEGL.eglChooseConfig(eglDisplay,
                                         egl_config_attribs,
                                         configs,
                                         configs_size,
                                         num_configs)) {
            throw new RuntimeException("eglChooseConfig() failed");
        }
        if (num_configs.getInt(0) == 0) {
            throw new RuntimeException("failed to find suitable EGLConfig");
        }
    }

    private Pointer createEglConfigAttribs() {
        final int     size          = (12 * 2) + 1;
        final Pointer configAttribs = new Memory(Integer.BYTES * size);
        configAttribs.write(0,
                            new int[]{
                                         //@formatter:off
                                         EGL_COLOR_BUFFER_TYPE, EGL_RGB_BUFFER,
                                         EGL_BUFFER_SIZE,       32,
                                         EGL_RED_SIZE,          8,
                                         EGL_GREEN_SIZE,        8,
                                         EGL_BLUE_SIZE,         8,
                                         EGL_ALPHA_SIZE,        8,
                                         EGL_DEPTH_SIZE,        24,
                                         EGL_STENCIL_SIZE,      8,
                                         EGL_SAMPLE_BUFFERS,    0,
                                         EGL_SAMPLES,           0,
                                         EGL_SURFACE_TYPE,      EGL_WINDOW_BIT,
                                         EGL_RENDERABLE_TYPE,   EGL_OPENGL_ES2_BIT,
                                         EGL_NONE
                                         //@formatter:on
                            },
                            0,
                            size);
        return configAttribs;
    }

    private Pointer createEglContext(final Pointer eglDisplay,
                                     final Pointer config) {
        final Pointer eglContextAttribs = createEglContextAttribs();
        final Pointer context = this.libEGL.eglCreateContext(eglDisplay,
                                                             config,
                                                             EGL_NO_CONTEXT,
                                                             eglContextAttribs);
        if (context == null) {
            throw new RuntimeException("eglCreateContext() failed");
        }
        return context;
    }

    private Pointer createEglContextAttribs() {
        final Pointer eglContextAttribs = new Memory(Integer.BYTES * 3);
        eglContextAttribs.write(0,
                                new int[]{
                                        EGL_CONTEXT_CLIENT_VERSION,
                                        2,
                                        EGL_NONE
                                },
                                0,
                                3);
        return eglContextAttribs;
    }

    private Pointer createEglSurface(final Pointer eglDisplay,
                                     final Pointer config,
                                     final Pointer context,
                                     final int nativeWindow) {
        final Pointer eglSurfaceAttribs = createSurfaceAttribs();
        final Memory  surfaceId         = new Memory(Integer.BYTES);
        surfaceId.setInt(0,
                         nativeWindow);
        final Pointer eglSurface = this.libEGL.eglCreatePlatformWindowSurfaceEXT(eglDisplay,
                                                                                 config,
                                                                                 surfaceId,
                                                                                 eglSurfaceAttribs);
        if (eglSurface == null) {
            throw new RuntimeException("eglCreateWindowSurface() failed");
        }
        if (!this.libEGL.eglMakeCurrent(eglDisplay,
                                        eglSurface,
                                        eglSurface,
                                        context)) {
            throw new RuntimeException("eglMakeCurrent() failed");
        }
        return eglSurface;
    }

    private Pointer createSurfaceAttribs() {
        final Pointer eglSurfaceAttribs = new Memory(3 * Integer.BYTES);
        eglSurfaceAttribs.write(0,
                                new int[]{
                                        EGL_RENDER_BUFFER,
                                        EGL_BACK_BUFFER,
                                        EGL_NONE
                                },
                                0,
                                3);
        return eglSurfaceAttribs;
    }
}
