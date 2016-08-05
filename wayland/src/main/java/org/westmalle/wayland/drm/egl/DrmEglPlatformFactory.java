//Copyright 2016 Erik De Rijcke
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
package org.westmalle.wayland.drm.egl;


import org.freedesktop.jaccall.Pointer;
import org.westmalle.wayland.core.GlRenderer;
import org.westmalle.wayland.drm.DrmConnector;
import org.westmalle.wayland.drm.DrmPlatform;
import org.westmalle.wayland.nativ.glibc.Libc;
import org.westmalle.wayland.nativ.libEGL.EglCreatePlatformWindowSurfaceEXT;
import org.westmalle.wayland.nativ.libEGL.EglGetPlatformDisplayEXT;
import org.westmalle.wayland.nativ.libEGL.LibEGL;
import org.westmalle.wayland.nativ.libGLESv2.LibGLESv2;
import org.westmalle.wayland.nativ.libdrm.Libdrm;
import org.westmalle.wayland.nativ.libgbm.Libgbm;
import org.westmalle.wayland.tty.Tty;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import static java.lang.String.format;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_BACK_BUFFER;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_CLIENT_APIS;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_CONTEXT_CLIENT_VERSION;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_EXTENSIONS;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_NONE;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_NO_CONTEXT;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_NO_DISPLAY;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_PLATFORM_GBM_KHR;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_RENDER_BUFFER;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_VENDOR;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_VERSION;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_COLOR_BUFFER_BIT;
import static org.westmalle.wayland.nativ.libgbm.Libgbm.GBM_BO_USE_RENDERING;
import static org.westmalle.wayland.nativ.libgbm.Libgbm.GBM_BO_USE_SCANOUT;
import static org.westmalle.wayland.nativ.libgbm.Libgbm.GBM_FORMAT_XRGB8888;

public class DrmEglPlatformFactory {

    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    @Nonnull
    private final PrivateDrmEglPlatformFactory privateDrmEglPlatformFactory;
    @Nonnull
    private final Libc                         libc;
    @Nonnull
    private final Libdrm                       libdrm;
    @Nonnull
    private final Libgbm                       libgbm;
    @Nonnull
    private final LibEGL                       libEGL;
    @Nonnull
    private final LibGLESv2                    libGLESv2;
    @Nonnull
    private final DrmPlatform                  drmPlatform;
    @Nonnull
    private final DrmEglConnectorFactory       drmEglConnectorFactory;
    @Nonnull
    private final GlRenderer                   glRenderer;
    @Nonnull
    private final Tty                          tty;

    @Inject
    DrmEglPlatformFactory(@Nonnull final PrivateDrmEglPlatformFactory privateDrmEglPlatformFactory,
                          @Nonnull final Libc libc,
                          @Nonnull final Libdrm libdrm,
                          @Nonnull final Libgbm libgbm,
                          @Nonnull final LibEGL libEGL,
                          @Nonnull final LibGLESv2 libGLESv2,
                          @Nonnull final DrmPlatform drmPlatform,
                          @Nonnull final DrmEglConnectorFactory drmEglConnectorFactory,
                          @Nonnull final GlRenderer glRenderer,
                          @Nonnull final Tty tty) {
        this.privateDrmEglPlatformFactory = privateDrmEglPlatformFactory;
        this.libc = libc;
        this.libdrm = libdrm;
        this.libgbm = libgbm;
        this.libEGL = libEGL;
        this.libGLESv2 = libGLESv2;
        this.drmPlatform = drmPlatform;
        this.drmEglConnectorFactory = drmEglConnectorFactory;
        this.glRenderer = glRenderer;
        this.tty = tty;
    }

    public DrmEglPlatform create() {
        final long gbmDevice  = this.libgbm.gbm_create_device(this.drmPlatform.getDrmFd());
        final long eglDisplay = createEglDisplay(gbmDevice);

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

        LOGGER.info(format("Creating DRM EGL output:\n"
                           + "\tEGL client apis: %s\n"
                           + "\tEGL vendor: %s\n"
                           + "\tEGL version: %s\n"
                           + "\tEGL extensions: %s",
                           eglClientApis,
                           eglVendor,
                           eglVersion,
                           eglExtensions));

        final long eglConfig = this.glRenderer.eglConfig(eglDisplay,
                                                         eglExtensions);
        final long eglContext = createEglContext(eglDisplay,
                                                 eglConfig);

        final List<Optional<DrmConnector>>    drmConnectors    = this.drmPlatform.getConnectors();
        final List<Optional<DrmEglConnector>> drmEglConnectors = new ArrayList<>(drmConnectors.size());

        drmConnectors.forEach(drmConnectorOptional ->
                                      drmEglConnectors.add(drmConnectorOptional.map(drmConnector ->
                                                                                            createGbmEglConnector(drmConnector,
                                                                                                                  gbmDevice,
                                                                                                                  eglDisplay,
                                                                                                                  eglContext,
                                                                                                                  eglConfig))));
        this.tty.getVtEnterSignal()
                .connect(event -> enterVt(drmEglConnectors));
        this.tty.getVtLeaveSignal()
                .connect(event -> leaveVt(drmEglConnectors));

        return this.privateDrmEglPlatformFactory.create(gbmDevice,
                                                        eglDisplay,
                                                        eglContext,
                                                        eglExtensions,
                                                        drmEglConnectors);
    }

    private void enterVt(final List<Optional<DrmEglConnector>> drmEglConnectors) {
        drmEglConnectors.forEach(optionalDrmEglConnector ->
                                         optionalDrmEglConnector.ifPresent((drmEglConnector) -> {
                                             drmEglConnector.setDefaultMode();
                                             drmEglConnector.enableDraw();
                                         }));
    }

    private void leaveVt(final List<Optional<DrmEglConnector>> drmEglConnectors) {
        drmEglConnectors.forEach(drmEglConnector -> drmEglConnector.ifPresent(DrmEglConnector::disableDraw));
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

    private DrmEglConnector createGbmEglConnector(final DrmConnector drmConnector,
                                                  final long gbmDevice,
                                                  final long eglDisplay,
                                                  final long eglContext,
                                                  final long eglConfig) {

        final long gbmSurface = this.libgbm.gbm_surface_create(gbmDevice,
                                                               drmConnector.getMode()
                                                                           .hdisplay(),
                                                               drmConnector.getMode()
                                                                           .vdisplay(),
                                                               GBM_FORMAT_XRGB8888,
                                                               GBM_BO_USE_SCANOUT | GBM_BO_USE_RENDERING);

        if (gbmSurface == 0) {
            throw new RuntimeException("failed to create gbm surface");
        }

        final long eglSurface = createEglSurface(eglDisplay,
                                                 eglConfig,
                                                 gbmSurface);

        this.libEGL.eglMakeCurrent(eglDisplay,
                                   eglSurface,
                                   eglSurface,
                                   eglContext);
        this.libGLESv2.glClearColor(1.0f,
                                    1.0f,
                                    1.0f,
                                    1.0f);
        this.libGLESv2.glClear(GL_COLOR_BUFFER_BIT);
        this.libEGL.eglSwapBuffers(eglDisplay,
                                   eglSurface);
        final long gbmBo = this.libgbm.gbm_surface_lock_front_buffer(gbmSurface);

        final DrmEglConnector drmEglConnector = this.drmEglConnectorFactory.create(this.drmPlatform.getDrmFd(),
                                                                                   gbmBo,
                                                                                   gbmSurface,
                                                                                   drmConnector,
                                                                                   eglSurface,
                                                                                   eglContext,
                                                                                   eglDisplay);
        drmEglConnector.setDefaultMode();

        return drmEglConnector;
    }

    private long createEglDisplay(final long gbmDevice) {

        final Pointer<String> noDisplayExtensions = Pointer.wrap(String.class,
                                                                 this.libEGL.eglQueryString(EGL_NO_DISPLAY,
                                                                                            EGL_EXTENSIONS));
        if (noDisplayExtensions.address == 0L) {
            throw new RuntimeException("Could not query egl extensions.");
        }
        final String extensions = noDisplayExtensions.dref();

        if (!extensions.contains("EGL_MESA_platform_gbm")) {
            throw new RuntimeException("Required extension EGL_MESA_platform_gbm not available.");
        }

        final Pointer<EglGetPlatformDisplayEXT> eglGetPlatformDisplayEXT = Pointer.wrap(EglGetPlatformDisplayEXT.class,
                                                                                        this.libEGL.eglGetProcAddress(Pointer.nref("eglGetPlatformDisplayEXT").address));

        final long eglDisplay = eglGetPlatformDisplayEXT.dref()
                                                        .$(EGL_PLATFORM_GBM_KHR,
                                                           gbmDevice,
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

    private long createEglSurface(final long eglDisplay,
                                  final long config,
                                  final long gbmSurface) {
        final Pointer<Integer> eglSurfaceAttribs = Pointer.nref(EGL_RENDER_BUFFER,
                                                                EGL_BACK_BUFFER,
                                                                EGL_NONE);

        final Pointer<EglCreatePlatformWindowSurfaceEXT> eglGetPlatformDisplayEXT = Pointer.wrap(EglCreatePlatformWindowSurfaceEXT.class,
                                                                                                 this.libEGL.eglGetProcAddress(Pointer.nref("eglCreatePlatformWindowSurfaceEXT").address));
        final long eglSurface = eglGetPlatformDisplayEXT.dref()
                                                        .$(eglDisplay,
                                                           config,
                                                           gbmSurface,
                                                           eglSurfaceAttribs.address);
        if (eglSurface == 0L) {
            throw new RuntimeException("eglCreateWindowSurface() failed");
        }

        return eglSurface;
    }
}
