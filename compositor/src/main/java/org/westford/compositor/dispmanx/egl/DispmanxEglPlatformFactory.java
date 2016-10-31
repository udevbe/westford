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
package org.westford.compositor.dispmanx.egl;

import org.freedesktop.jaccall.Pointer;
import org.westford.compositor.core.GlRenderer;
import org.westford.compositor.dispmanx.DispmanxOutput;
import org.westford.compositor.dispmanx.DispmanxPlatform;
import org.westford.nativ.libEGL.LibEGL;
import org.westford.nativ.libbcm_host.DISPMANX_MODEINFO_T;
import org.westford.nativ.libbcm_host.EGL_DISPMANX_WINDOW_T;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static java.lang.String.format;
import static org.westford.nativ.libEGL.LibEGL.EGL_CLIENT_APIS;
import static org.westford.nativ.libEGL.LibEGL.EGL_CONTEXT_CLIENT_VERSION;
import static org.westford.nativ.libEGL.LibEGL.EGL_EXTENSIONS;
import static org.westford.nativ.libEGL.LibEGL.EGL_NONE;
import static org.westford.nativ.libEGL.LibEGL.EGL_NO_CONTEXT;
import static org.westford.nativ.libEGL.LibEGL.EGL_NO_DISPLAY;
import static org.westford.nativ.libEGL.LibEGL.EGL_NO_SURFACE;
import static org.westford.nativ.libEGL.LibEGL.EGL_VENDOR;
import static org.westford.nativ.libEGL.LibEGL.EGL_VERSION;

public class DispmanxEglPlatformFactory {

    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    @Nonnull
    private final LibEGL                            libEGL;
    @Nonnull
    private final PrivateDispmanxEglPlatformFactory privateDispmanxEglOutputFactory;
    @Nonnull
    private final DispmanxEglOutputFactory          dispmanxEglRenderOutputFactory;
    @Nonnull
    private final DispmanxPlatform                  dispmanxPlatform;
    @Nonnull
    private final GlRenderer                        glRenderer;

    @Inject
    DispmanxEglPlatformFactory(@Nonnull final LibEGL libEGL,
                               @Nonnull final PrivateDispmanxEglPlatformFactory privateDispmanxEglOutputFactory,
                               @Nonnull final DispmanxEglOutputFactory dispmanxEglRenderOutputFactory,
                               @Nonnull final DispmanxPlatform dispmanxPlatform,
                               @Nonnull final GlRenderer glRenderer) {
        this.libEGL = libEGL;
        this.privateDispmanxEglOutputFactory = privateDispmanxEglOutputFactory;
        this.dispmanxEglRenderOutputFactory = dispmanxEglRenderOutputFactory;
        this.dispmanxPlatform = dispmanxPlatform;
        this.glRenderer = glRenderer;
    }

    public DispmanxEglPlatform create() {

        final DISPMANX_MODEINFO_T modeinfo = this.dispmanxPlatform.getModeinfo();


        final long nativeDisplay = LibEGL.EGL_DEFAULT_DISPLAY;
        final long eglDisplay    = createEglDisplay(nativeDisplay);

        final String eglExtensions = Pointer.wrap(String.class,
                                                  this.libEGL.eglQueryString(eglDisplay,
                                                                             EGL_EXTENSIONS))
                                            .dref();
        final String eglClientApis = Pointer.wrap(String.class,
                                                  this.libEGL.eglQueryString(eglDisplay,
                                                                             EGL_CLIENT_APIS))
                                            .dref();
        final String eglVendor = Pointer.wrap(String.class,
                                              this.libEGL.eglQueryString(eglDisplay,
                                                                         EGL_VENDOR))
                                        .dref();
        final String eglVersion = Pointer.wrap(String.class,
                                               this.libEGL.eglQueryString(eglDisplay,
                                                                          EGL_VERSION))
                                         .dref();
        LOGGER.info(format("Creating dispmanx EGL output:\n"
                           + "\tEGL client apis: %s\n"
                           + "\tEGL vendor: %s\n"
                           + "\tEGL version: %s\n"
                           + "\tEGL extensions: %s",
                           eglClientApis,
                           eglVendor,
                           eglVersion,
                           eglExtensions));

        final long config = this.glRenderer.eglConfig(eglDisplay,
                                                      eglExtensions);
        // create an EGL rendering eglContext
        final long eglContext = getContext(eglDisplay,
                                           config);

        final List<DispmanxOutput>    dispmanxOutputs          = this.dispmanxPlatform.getRenderOutputs();
        final List<DispmanxEglOutput> dispmanxEglRenderOutputs = new ArrayList<>(dispmanxOutputs.size());

        dispmanxOutputs.forEach(dispmanxOutput -> {
            final int                   dispmanxElement   = dispmanxOutput.getDispmanxElement();
            final EGL_DISPMANX_WINDOW_T eglDispmanxWindow = new EGL_DISPMANX_WINDOW_T();
            eglDispmanxWindow.element(dispmanxElement);
            eglDispmanxWindow.width(modeinfo.width());
            eglDispmanxWindow.height(modeinfo.height());

            final long eglSurface = createEglSurface(Pointer.ref(eglDispmanxWindow).address,
                                                     eglDisplay,
                                                     config,
                                                     eglContext);

            dispmanxEglRenderOutputs.add(this.dispmanxEglRenderOutputFactory.create(dispmanxOutput,
                                                                                    eglDispmanxWindow,
                                                                                    eglSurface,
                                                                                    eglContext,
                                                                                    eglDisplay));
        });

        return this.privateDispmanxEglOutputFactory.create(this.dispmanxPlatform,
                                                           dispmanxEglRenderOutputs,
                                                           eglDisplay,
                                                           eglContext,
                                                           eglExtensions);
    }

    private long createEglDisplay(final long nativeDisplay) {
        final long display = this.libEGL.eglGetDisplay(nativeDisplay);
        if (display == EGL_NO_DISPLAY) {
            this.libEGL.throwError("eglGetDisplay");
        }

        // initialize the EGL display connection
        if (this.libEGL.eglInitialize(display,
                                      0L,
                                      0L) != 0) {
            this.libEGL.throwError("eglInitialize");
        }

        return display;
    }

    private long getContext(final long display,
                            final long config) {
        final long context = this.libEGL.eglCreateContext(display,
                                                          config,
                                                          EGL_NO_CONTEXT,
                                                          Pointer.nref(EGL_CONTEXT_CLIENT_VERSION,
                                                                       2,
                                                                       EGL_NONE).address);
        if (context == EGL_NO_CONTEXT) {
            this.libEGL.throwError("eglCreateContext");
        }
        return context;
    }

    private long createEglSurface(final long nativewindow,
                                  final long display,
                                  final long config,
                                  final long context) {

        final long surface = this.libEGL.eglCreateWindowSurface(display,
                                                                config,
                                                                nativewindow,
                                                                0L);
        if (surface == EGL_NO_SURFACE) {
            this.libEGL.throwError("eglCreateWindowSurface");
        }

        // connect the context to the surface
        if (this.libEGL.eglMakeCurrent(display,
                                       surface,
                                       surface,
                                       context) != 0) {
            this.libEGL.throwError("eglMakeCurrent");
        }

        return surface;
    }
}
