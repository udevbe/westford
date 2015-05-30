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
import org.westmalle.wayland.nativ.Libegl;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import static org.westmalle.wayland.nativ.Libegl.*;

public class XEglOutputFactory {

    @Nonnull
    private final Libegl libegl;

    @Inject
    XEglOutputFactory(@Nonnull final Libegl libegl) {
        this.libegl = libegl;
    }

    @Nonnull
    public XEglOutput create(@Nonnull final Pointer display,
                             final int window) {
        if (!this.libegl.eglBindAPI(EGL_OPENGL_ES_API)) {
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
        return new XEglOutput(this.libegl,
                              eglDisplay,
                              createEglSurface(eglDisplay,
                                               config,
                                               context,
                                               window),
                              context);
    }

    private Pointer createEglDisplay(final Pointer nativeDisplay) {
        final Pointer eglDisplay = this.libegl.eglGetPlatformDisplayEXT(EGL_PLATFORM_X11_KHR,
                                                                        nativeDisplay,
                                                                        null);
        if (eglDisplay == EGL_NO_DISPLAY) {
            throw new RuntimeException("eglGetDisplay() failed");
        }
        if (!this.libegl.eglInitialize(eglDisplay,
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
        if (!this.libegl.eglChooseConfig(eglDisplay,
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
        final Pointer egl_config_attribs = new Memory(Integer.BYTES * ((12 * 2) + 1));
        //TODO just write an int array...
        egl_config_attribs.setInt(0,
                                  EGL_COLOR_BUFFER_TYPE);
        egl_config_attribs.setInt(4,
                                  EGL_RGB_BUFFER);

        egl_config_attribs.setInt(8,
                                  EGL_BUFFER_SIZE);
        egl_config_attribs.setInt(12,
                                  32);

        egl_config_attribs.setInt(16,
                                  EGL_RED_SIZE);
        egl_config_attribs.setInt(20,
                                  8);

        egl_config_attribs.setInt(24,
                                  EGL_GREEN_SIZE);
        egl_config_attribs.setInt(28,
                                  8);

        egl_config_attribs.setInt(32,
                                  EGL_BLUE_SIZE);
        egl_config_attribs.setInt(36,
                                  8);

        egl_config_attribs.setInt(40,
                                  EGL_ALPHA_SIZE);
        egl_config_attribs.setInt(44,
                                  8);

        egl_config_attribs.setInt(48,
                                  EGL_DEPTH_SIZE);
        egl_config_attribs.setInt(52,
                                  24);

        egl_config_attribs.setInt(56,
                                  EGL_STENCIL_SIZE);
        egl_config_attribs.setInt(60,
                                  8);

        egl_config_attribs.setInt(64,
                                  EGL_SAMPLE_BUFFERS);
        egl_config_attribs.setInt(68,
                                  0);

        egl_config_attribs.setInt(72,
                                  EGL_SAMPLES);
        egl_config_attribs.setInt(76,
                                  0);

        egl_config_attribs.setInt(80,
                                  EGL_SURFACE_TYPE);
        egl_config_attribs.setInt(84,
                                  EGL_WINDOW_BIT);

        egl_config_attribs.setInt(88,
                                  EGL_RENDERABLE_TYPE);
        egl_config_attribs.setInt(92,
                                  EGL_OPENGL_ES2_BIT);

        egl_config_attribs.setInt(96,
                                  EGL_NONE);

        return egl_config_attribs;
    }

    private Pointer createEglContext(final Pointer eglDisplay,
                                     final Pointer config) {
        final Pointer eglContextAttribs = createEglContextAttribs();
        final Pointer context = this.libegl.eglCreateContext(eglDisplay,
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
        //TODO just write an int array...
        eglContextAttribs.setInt(0,
                                 EGL_CONTEXT_CLIENT_VERSION);
        eglContextAttribs.setInt(4,
                                 2);
        eglContextAttribs.setInt(8,
                                 EGL_NONE);
        return eglContextAttribs;
    }

    private Pointer createEglSurface(final Pointer eglDisplay,
                                     final Pointer config,
                                     final Pointer context,
                                     final int nativeWindow) {
        final Pointer eglSurfaceAttribs = createSurfaceAttribs();
        final Pointer eglSurface = this.libegl.eglCreatePlatformWindowSurfaceEXT(eglDisplay,
                                                                                 config,
                                                                                 Pointer.createConstant(nativeWindow),
                                                                                 eglSurfaceAttribs);
        if (eglSurface == null) {
            throw new RuntimeException("eglCreateWindowSurface() failed");
        }
        if (!this.libegl.eglMakeCurrent(eglDisplay,
                                        eglSurface,
                                        eglSurface,
                                        context)) {
            throw new RuntimeException("eglMakeCurrent() failed");
        }
        return eglSurface;
    }

    private Pointer createSurfaceAttribs() {
        final Pointer eglSurfaceAttribs = new Memory(3 * Integer.BYTES);
        //TODO just write an int array...
        eglSurfaceAttribs.setInt(0,
                                 EGL_RENDER_BUFFER);
        eglSurfaceAttribs.setInt(4,
                                 EGL_BACK_BUFFER);
        eglSurfaceAttribs.setInt(12,
                                 EGL_NONE);
        return eglSurfaceAttribs;
    }
}
