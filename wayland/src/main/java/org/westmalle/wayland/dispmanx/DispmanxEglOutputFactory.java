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
package org.westmalle.wayland.dispmanx;


import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import org.westmalle.wayland.nativ.libEGL.LibEGL;
import org.westmalle.wayland.nativ.libbcm_host.EGL_DISPMANX_WINDOW_T;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.logging.Logger;

import static java.lang.String.format;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_ALPHA_SIZE;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_BLUE_SIZE;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_CLIENT_APIS;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_CONTEXT_CLIENT_VERSION;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_DEFAULT_DISPLAY;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_GREEN_SIZE;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_NONE;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_NO_CONTEXT;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_NO_DISPLAY;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_OPENGL_ES_API;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_RED_SIZE;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_SURFACE_TYPE;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_VENDOR;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_VERSION;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_WINDOW_BIT;

//TODO unit test
public class DispmanxEglOutputFactory {

    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    @Nonnull
    private final PrivateDispmanxEglOutputFactory privateDispmanxEglOutputFactory;
    @Nonnull
    private final LibEGL                          libEGL;

    @Inject
    DispmanxEglOutputFactory(@Nonnull final PrivateDispmanxEglOutputFactory privateDispmanxEglOutputFactory,
                             @Nonnull final LibEGL libEGL) {
        this.privateDispmanxEglOutputFactory = privateDispmanxEglOutputFactory;
        this.libEGL = libEGL;
    }

    public DispmanxEglOutput create(final EGL_DISPMANX_WINDOW_T dispmanxWindow) {

        if (!this.libEGL.eglBindAPI(EGL_OPENGL_ES_API)) {
            throw new RuntimeException("eglBindAPI failed");
        }
        final Pointer eglDisplay = createEglDisplay();

        final int     configs_size = 256 * Pointer.SIZE;
        final Pointer configs      = new Memory(configs_size);
        chooseConfig(eglDisplay,
                     configs,
                     configs_size);
        final Pointer config = configs.getPointer(0);

        final Pointer context = createEglContext(eglDisplay,
                                                 config);

        final Pointer eglSurface = createEglSurface(eglDisplay,
                                                    config,
                                                    context,
                                                    dispmanxWindow);

        return this.privateDispmanxEglOutputFactory.create(eglDisplay,
                                                           eglSurface,
                                                           context);
    }

    private Pointer createEglDisplay() {

        Pointer eglDisplay = this.libEGL.eglGetDisplay(EGL_DEFAULT_DISPLAY);

        if (eglDisplay == null || eglDisplay.equals(EGL_NO_DISPLAY)) {
            throw new RuntimeException("eglGetDisplay() failed");
        }
        if (!this.libEGL.eglInitialize(eglDisplay,
                                       null,
                                       null)) {
            throw new RuntimeException("eglInitialize() failed");
        }

        final String eglClientApis = this.libEGL.eglQueryString(eglDisplay,
                                                                EGL_CLIENT_APIS)
                                                .getString(0);
        final String eglVendor = this.libEGL.eglQueryString(eglDisplay,
                                                            EGL_VENDOR)
                                            .getString(0);
        final String eglVersion = this.libEGL.eglQueryString(eglDisplay,
                                                             EGL_VERSION)
                                             .getString(0);

        LOGGER.info(format("Creating Dispmanx EGL output:\n"
                           + "\tEGL client apis: %s\n"
                           + "\tEGL vendor: %s\n"
                           + "\tEGL version: %s\n",
                           eglClientApis,
                           eglVendor,
                           eglVersion));

        return eglDisplay;
    }

    private Pointer createEglSurface(final Pointer eglDisplay,
                                     final Pointer config,
                                     final Pointer context,
                                     final EGL_DISPMANX_WINDOW_T nativeWindow) {
        final Pointer eglSurface = libEGL.eglCreateWindowSurface(eglDisplay,
                                                                 config,
                                                                 nativeWindow.getPointer(),
                                                                 null);
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

    private Pointer createEglContextAttribs() {
        final int[] contextAttributes = {
                //@formatter:off
                EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL_NONE
                //@formatter:on
        };
        final Pointer eglContextAttribs = new Memory(Integer.BYTES * contextAttributes.length);
        eglContextAttribs.write(0,
                                contextAttributes,
                                0,
                                contextAttributes.length);
        return eglContextAttribs;
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
        final int[] attributes = {
                //@formatter:off
                EGL_RED_SIZE, 8,
                EGL_GREEN_SIZE, 8,
                EGL_BLUE_SIZE, 8,
                EGL_ALPHA_SIZE, 8,
                EGL_SURFACE_TYPE, EGL_WINDOW_BIT,
                EGL_NONE
                //@formatter:on
        };
        final Pointer configAttribs = new Memory(Integer.BYTES * attributes.length);
        configAttribs.write(0,
                            attributes,
                            0,
                            attributes.length);
        return configAttribs;
    }
}
