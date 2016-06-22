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
package org.westmalle.wayland.x11.egl;

import org.freedesktop.jaccall.Pointer;
import org.westmalle.wayland.core.GlRenderer;
import org.westmalle.wayland.nativ.libEGL.EglCreatePlatformWindowSurfaceEXT;
import org.westmalle.wayland.nativ.libEGL.EglGetPlatformDisplayEXT;
import org.westmalle.wayland.nativ.libEGL.LibEGL;
import org.westmalle.wayland.x11.X11Connector;
import org.westmalle.wayland.x11.X11Platform;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.logging.Logger;

import static java.lang.String.format;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_BACK_BUFFER;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_CLIENT_APIS;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_CONTEXT_CLIENT_VERSION;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_EXTENSIONS;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_NONE;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_NO_CONTEXT;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_NO_DISPLAY;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_OPENGL_ES_API;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_PLATFORM_X11_KHR;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_RENDER_BUFFER;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_VENDOR;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_VERSION;

public class X11EglPlatformFactory {

    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    @Nonnull
    private final LibEGL                       libEGL;
    @Nonnull
    private final PrivateX11EglPlatformFactory privateX11EglOutputFactory;
    @Nonnull
    private final X11Platform                  x11Platform;
    @Nonnull
    private final GlRenderer                   glRenderer;
    @Nonnull
    private final X11EglConnectorFactory       x11EglConnectorFactory;

    @Inject
    X11EglPlatformFactory(@Nonnull final LibEGL libEGL,
                          @Nonnull final PrivateX11EglPlatformFactory privateX11EglOutputFactory,
                          @Nonnull final X11Platform x11Platform,
                          @Nonnull final GlRenderer glRenderer,
                          @Nonnull final X11EglConnectorFactory x11EglConnectorFactory) {
        this.libEGL = libEGL;
        this.privateX11EglOutputFactory = privateX11EglOutputFactory;
        this.x11Platform = x11Platform;
        this.glRenderer = glRenderer;
        this.x11EglConnectorFactory = x11EglConnectorFactory;
    }

    @Nonnull
    public X11EglPlatform create() {

        if (this.libEGL.eglBindAPI(EGL_OPENGL_ES_API) == 0L) {
            throw new RuntimeException("eglBindAPI failed");
        }

        final long eglDisplay = createEglDisplay(this.x11Platform.getxDisplay());

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

        LOGGER.info(format("Creating X11 EGL output:\n"
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
        final long eglContext = createEglContext(eglDisplay,
                                                 config);

        final X11Connector[]    x11Connectors    = this.x11Platform.getConnectors();
        final X11EglConnector[] x11EglConnectors = new X11EglConnector[x11Connectors.length];

        for (int i = 0, x11ConnectorsLength = x11Connectors.length; i < x11ConnectorsLength; i++) {
            final X11Connector x11Connector = x11Connectors[i];
            final long eglSurface = createEglSurface(eglDisplay,
                                                     config,
                                                     eglContext,
                                                     x11Connector.getXWindow());
            final X11EglConnector x11EglConnector = this.x11EglConnectorFactory.create(x11Connector,
                                                                                       eglSurface);
            x11EglConnectors[i] = x11EglConnector;
        }

        return this.privateX11EglOutputFactory.create(this.x11Platform,
                                                      x11EglConnectors,
                                                      eglDisplay,
                                                      eglContext,
                                                      eglExtensions);
    }

    private long createEglDisplay(final long nativeDisplay) {

        final Pointer<String> noDisplayExtensions = Pointer.wrap(String.class,
                                                                 this.libEGL.eglQueryString(EGL_NO_DISPLAY,
                                                                                            EGL_EXTENSIONS));
        if (noDisplayExtensions.address == 0L) {
            throw new RuntimeException("Could not query egl extensions.");
        }
        final String extensions = noDisplayExtensions.dref();

        if (!extensions.contains("EGL_EXT_platform_x11")) {
            throw new RuntimeException("Required extension EGL_EXT_platform_x11 not available.");
        }

        final Pointer<EglGetPlatformDisplayEXT> eglGetPlatformDisplayEXT = Pointer.wrap(EglGetPlatformDisplayEXT.class,
                                                                                        this.libEGL.eglGetProcAddress(Pointer.nref("eglGetPlatformDisplayEXT").address));

        final long eglDisplay = eglGetPlatformDisplayEXT.dref()
                                                        .$(EGL_PLATFORM_X11_KHR,
                                                           nativeDisplay,
                                                           0L);
        if (eglDisplay == 0L) {
            throw new RuntimeException("eglGetDisplay() failed");
        }
        if (this.libEGL.eglInitialize(eglDisplay,
                                      0L,
                                      0L) == 0) {
            throw new RuntimeException("eglInitialize() failed");
        }

        return eglDisplay;
    }

    private long createEglContext(final long eglDisplay,
                                  final long config) {
        final Pointer<?> eglContextAttribs = Pointer.nref(
                //@formatter:off
                EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL_NONE
                //@formatter:on
                                                         );
        final long context = this.libEGL.eglCreateContext(eglDisplay,
                                                          config,
                                                          EGL_NO_CONTEXT,
                                                          eglContextAttribs.address);
        if (context == 0L) {
            throw new RuntimeException("eglCreateContext() failed");
        }
        return context;
    }

    public long createEglSurface(final long eglDisplay,
                                 final long config,
                                 final long context,
                                 final int nativeWindow) {
        final Pointer<Integer> eglSurfaceAttribs = Pointer.nref(EGL_RENDER_BUFFER,
                                                                EGL_BACK_BUFFER,
                                                                EGL_NONE);

        final Pointer<EglCreatePlatformWindowSurfaceEXT> eglGetPlatformDisplayEXT = Pointer.wrap(EglCreatePlatformWindowSurfaceEXT.class,
                                                                                                 this.libEGL.eglGetProcAddress(Pointer.nref("eglCreatePlatformWindowSurfaceEXT").address));
        final long eglSurface = eglGetPlatformDisplayEXT.dref()
                                                        .$(eglDisplay,
                                                           config,
                                                           Pointer.nref(nativeWindow).address,
                                                           eglSurfaceAttribs.address);
        if (eglSurface == 0L) {
            throw new RuntimeException("eglCreateWindowSurface() failed");
        }
        if (this.libEGL.eglMakeCurrent(eglDisplay,
                                       eglSurface,
                                       eglSurface,
                                       context) == 0L) {
            throw new RuntimeException("eglMakeCurrent() failed");
        }
        return eglSurface;
    }
}
