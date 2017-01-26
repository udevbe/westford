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
package org.westford.compositor.drm.egl;


import org.freedesktop.jaccall.Pointer;
import org.westford.compositor.core.GlRenderer;
import org.westford.compositor.core.OutputFactory;
import org.westford.compositor.core.OutputGeometry;
import org.westford.compositor.core.OutputMode;
import org.westford.compositor.drm.DrmOutput;
import org.westford.compositor.drm.DrmPlatform;
import org.westford.compositor.protocol.WlOutput;
import org.westford.compositor.protocol.WlOutputFactory;
import org.westford.launch.LifeCycleSignals;
import org.westford.launch.Privileges;
import org.westford.nativ.libEGL.EglCreatePlatformWindowSurfaceEXT;
import org.westford.nativ.libEGL.EglGetPlatformDisplayEXT;
import org.westford.nativ.libEGL.LibEGL;
import org.westford.nativ.libGLESv2.LibGLESv2;
import org.westford.nativ.libdrm.DrmModeConnector;
import org.westford.nativ.libdrm.DrmModeModeInfo;
import org.westford.nativ.libgbm.Libgbm;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static java.lang.String.format;
import static org.westford.nativ.libEGL.LibEGL.EGL_BACK_BUFFER;
import static org.westford.nativ.libEGL.LibEGL.EGL_CLIENT_APIS;
import static org.westford.nativ.libEGL.LibEGL.EGL_CONTEXT_CLIENT_VERSION;
import static org.westford.nativ.libEGL.LibEGL.EGL_EXTENSIONS;
import static org.westford.nativ.libEGL.LibEGL.EGL_NONE;
import static org.westford.nativ.libEGL.LibEGL.EGL_NO_CONTEXT;
import static org.westford.nativ.libEGL.LibEGL.EGL_NO_DISPLAY;
import static org.westford.nativ.libEGL.LibEGL.EGL_PLATFORM_GBM_KHR;
import static org.westford.nativ.libEGL.LibEGL.EGL_RENDER_BUFFER;
import static org.westford.nativ.libEGL.LibEGL.EGL_VENDOR;
import static org.westford.nativ.libEGL.LibEGL.EGL_VERSION;

public class DrmEglPlatformFactory {

    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    @Nonnull
    private final WlOutputFactory              wlOutputFactory;
    @Nonnull
    private final OutputFactory                outputFactory;
    @Nonnull
    private final PrivateDrmEglPlatformFactory privateDrmEglPlatformFactory;
    @Nonnull
    private final Libgbm                       libgbm;
    @Nonnull
    private final LibEGL                       libEGL;
    @Nonnull
    private final LibGLESv2                    libGLESv2;
    @Nonnull
    private final DrmPlatform                  drmPlatform;
    @Nonnull
    private final DrmEglOutputFactory          drmEglOutputFactory;
    @Nonnull
    private final GlRenderer                   glRenderer;
    @Nonnull
    private final LifeCycleSignals             lifeCycleSignals;
    @Nonnull
    private final Privileges                   privileges;

    @Inject
    DrmEglPlatformFactory(@Nonnull final WlOutputFactory wlOutputFactory,
                          @Nonnull final OutputFactory outputFactory,
                          @Nonnull final PrivateDrmEglPlatformFactory privateDrmEglPlatformFactory,
                          @Nonnull final Libgbm libgbm,
                          @Nonnull final LibEGL libEGL,
                          @Nonnull final LibGLESv2 libGLESv2,
                          @Nonnull final DrmPlatform drmPlatform,
                          @Nonnull final DrmEglOutputFactory drmEglOutputFactory,
                          @Nonnull final GlRenderer glRenderer,
                          @Nonnull final LifeCycleSignals lifeCycleSignals,
                          @Nonnull final Privileges privileges) {
        this.wlOutputFactory = wlOutputFactory;
        this.outputFactory = outputFactory;
        this.privateDrmEglPlatformFactory = privateDrmEglPlatformFactory;
        this.libgbm = libgbm;
        this.libEGL = libEGL;
        this.libGLESv2 = libGLESv2;
        this.drmPlatform = drmPlatform;
        this.drmEglOutputFactory = drmEglOutputFactory;
        this.glRenderer = glRenderer;
        this.lifeCycleSignals = lifeCycleSignals;
        this.privileges = privileges;
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

        final List<DrmOutput>    drmOutputs          = this.drmPlatform.getRenderOutputs();
        final List<DrmEglOutput> drmEglRenderOutputs = new ArrayList<>(drmOutputs.size());
        final List<WlOutput>     wlOutputs           = new ArrayList<>(drmEglRenderOutputs.size());

        drmOutputs.forEach(drmOutput ->
                                   drmEglRenderOutputs.add(createDrmEglRenderOutput(drmOutput,
                                                                                    gbmDevice,
                                                                                    eglDisplay,
                                                                                    eglContext,
                                                                                    eglConfig)));
        drmEglRenderOutputs.forEach(drmEglOutput ->
                                            wlOutputs.add(createWlOutput(drmEglOutput)));

        this.lifeCycleSignals.getActivateSignal()
                             .connect(event -> {
                                 this.privileges.setDrmMaster(this.drmPlatform.getDrmFd());
                                 wlOutputs.forEach(wlOutput -> {
                                     DrmEglOutput drmEglOutput = (DrmEglOutput) wlOutput.getOutput()
                                                                                        .getRenderOutput();
                                     drmEglOutput.setDefaultMode();
                                     drmEglOutput.enable(wlOutput);
                                 });
                             });
        this.lifeCycleSignals.getDeactivateSignal()
                             .connect(event -> {
                                 drmEglRenderOutputs.forEach(DrmEglOutput::disable);
                                 this.privileges.dropDrmMaster(this.drmPlatform.getDrmFd());
                             });


        return this.privateDrmEglPlatformFactory.create(gbmDevice,
                                                        eglDisplay,
                                                        eglContext,
                                                        eglExtensions,
                                                        wlOutputs);
    }

    private WlOutput createWlOutput(DrmEglOutput drmEglOutput) {

        final DrmOutput drmOutput = drmEglOutput.getDrmOutput();

        final DrmModeConnector drmModeConnector = drmOutput.getDrmModeConnector();
        final DrmModeModeInfo  drmModeModeInfo  = drmOutput.getMode();

        final int fallBackDpi = 96;

        int mmWidth = drmModeConnector.mmWidth();
        final short hdisplay = drmOutput.getMode()
                                        .hdisplay();
        if (mmWidth == 0) {
            mmWidth = (int) ((hdisplay * 25.4) / fallBackDpi);
        }

        int mmHeight = drmModeConnector.mmHeight();
        final short vdisplay = drmOutput.getMode()
                                        .vdisplay();
        if (mmHeight == 0) {
            mmHeight = (int) ((vdisplay * 25.4) / fallBackDpi);
        }

        //TODO gather more geo & drmModeModeInfo info
        final OutputGeometry outputGeometry = OutputGeometry.builder()
                                                            .physicalWidth(mmWidth)
                                                            .physicalHeight(mmHeight)
                                                            .make("unknown")
                                                            .model("unknown")
                                                            .x(0)
                                                            .y(0)
                                                            .subpixel(drmModeConnector.drmModeSubPixel())
                                                            .transform(0)
                                                            .build();
        final OutputMode outputMode = OutputMode.builder()
                                                .width(hdisplay)
                                                .height(vdisplay)
                                                .refresh(drmOutput.getMode()
                                                                  .vrefresh())
                                                .flags(drmModeModeInfo.flags())
                                                .build();

        //FIXME deduce an output name from the drm connector
        return this.wlOutputFactory.create(this.outputFactory.create(drmEglOutput,
                                                                     "fixme",
                                                                     outputGeometry,
                                                                     outputMode));
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

    private DrmEglOutput createDrmEglRenderOutput(final DrmOutput drmOutput,
                                                  final long gbmDevice,
                                                  final long eglDisplay,
                                                  final long eglContext,
                                                  final long eglConfig) {

        final DrmModeModeInfo drmModeModeInfo = drmOutput.getMode();

        final long gbmSurface = this.libgbm.gbm_surface_create(gbmDevice,
                                                               drmModeModeInfo
                                                                       .hdisplay(),
                                                               drmModeModeInfo
                                                                       .vdisplay(),
                                                               Libgbm.GBM_FORMAT_XRGB8888,
                                                               Libgbm.GBM_BO_USE_SCANOUT | Libgbm.GBM_BO_USE_RENDERING);

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
        this.libGLESv2.glClear(LibGLESv2.GL_COLOR_BUFFER_BIT);
        this.libEGL.eglSwapBuffers(eglDisplay,
                                   eglSurface);
        final long gbmBo = this.libgbm.gbm_surface_lock_front_buffer(gbmSurface);

        final DrmEglOutput drmEglRenderOutput = this.drmEglOutputFactory.create(this.drmPlatform.getDrmFd(),
                                                                                gbmBo,
                                                                                gbmSurface,
                                                                                drmOutput,
                                                                                eglSurface,
                                                                                eglContext,
                                                                                eglDisplay);
        drmEglRenderOutput.setDefaultMode();

        return drmEglRenderOutput;
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
