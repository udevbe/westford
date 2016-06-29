package org.westmalle.wayland.drm.egl;


import org.freedesktop.jaccall.Pointer;
import org.westmalle.wayland.drm.DrmConnector;
import org.westmalle.wayland.drm.DrmPlatform;
import org.westmalle.wayland.nativ.libdrm.DrmEventContext;
import org.westmalle.wayland.nativ.libdrm.Libdrm;
import org.westmalle.wayland.nativ.libdrm.Pointerpage_flip_handler;
import org.westmalle.wayland.nativ.libgbm.Libgbm;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import static org.westmalle.wayland.nativ.libgbm.Libgbm.GBM_BO_USE_RENDERING;
import static org.westmalle.wayland.nativ.libgbm.Libgbm.GBM_BO_USE_SCANOUT;
import static org.westmalle.wayland.nativ.libgbm.Libgbm.GBM_FORMAT_XRGB8888;

public class GbmEglPlatformFactory {

    @Nonnull
    private final PrivateGbmEglPlatformFactory privateGbmEglPlatformFactory;
    @Nonnull
    private final Libgbm                       libgbm;
    @Nonnull
    private final DrmPlatform                  drmPlatform;
    @Nonnull
    private final GbmEglConnectorFactory       eglGbmConnectorFactory;

    @Inject
    GbmEglPlatformFactory(@Nonnull final PrivateGbmEglPlatformFactory privateGbmEglPlatformFactory,
                          @Nonnull final Libgbm libgbm,
                          @Nonnull final DrmPlatform drmPlatform,
                          @Nonnull final GbmEglConnectorFactory eglGbmConnectorFactory) {
        this.privateGbmEglPlatformFactory = privateGbmEglPlatformFactory;
        this.libgbm = libgbm;
        this.drmPlatform = drmPlatform;
        this.eglGbmConnectorFactory = eglGbmConnectorFactory;
    }

    public GbmEglPlatform create() {
        final long gbmDevice = this.libgbm.gbm_create_device(this.drmPlatform.getDrmFd());

        final DrmConnector[]    drmConnectors    = this.drmPlatform.getConnectors();
        final GbmEglConnector[] gbmEglConnectors = new GbmEglConnector[drmConnectors.length];

        for (int i = 0; i < drmConnectors.length; i++) {
            final DrmConnector drmConnector = drmConnectors[i];

            final long gbmSurface = this.libgbm.gbm_surface_create(gbmDevice,
                                                                   drmConnector.getMode()
                                                                               .hdisplay(),
                                                                   drmConnector.getMode()
                                                                               .vdisplay(),
                                                                   GBM_FORMAT_XRGB8888,
                                                                   GBM_BO_USE_SCANOUT | GBM_BO_USE_RENDERING);

            //setup page flipping mechanism
            final Pointer<DrmEventContext> drmEventContextP = Pointer.malloc(DrmEventContext.SIZE,
                                                                             DrmEventContext.class);
            final DrmEventContext drmEventContext = drmEventContextP.dref();
            drmEventContext.version(Libdrm.DRM_EVENT_CONTEXT_VERSION);
            drmEventContext.page_flip_handler(Pointerpage_flip_handler.nref(this::page_flip_handler));

            gbmEglConnectors[i] = this.eglGbmConnectorFactory.create(this.drmPlatform.getDrmFd(),
                                                                     gbmSurface,
                                                                     fbId,
                                                                     pageFlipHandler,
                                                                     drmConnector);
        }


        this.privateGbmEglPlatformFactory.create(gbmDevice,
                                                 eglDisplay,
                                                 eglContext,
                                                 eglExtensions,
                                                 gbmEglConnectors);

    }
}
