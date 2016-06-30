package org.westmalle.wayland.drm.egl;


import org.freedesktop.jaccall.Pointer;
import org.freedesktop.jaccall.Ptr;
import org.freedesktop.jaccall.Size;
import org.freedesktop.jaccall.Unsigned;
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
    private final Libdrm                       libdrm;
    @Nonnull
    private final Libgbm                       libgbm;
    @Nonnull
    private final DrmPlatform                  drmPlatform;
    @Nonnull
    private final GbmEglConnectorFactory       eglGbmConnectorFactory;

    @Inject
    GbmEglPlatformFactory(@Nonnull final PrivateGbmEglPlatformFactory privateGbmEglPlatformFactory,
                          @Nonnull final Libdrm libdrm,
                          @Nonnull final Libgbm libgbm,
                          @Nonnull final DrmPlatform drmPlatform,
                          @Nonnull final GbmEglConnectorFactory eglGbmConnectorFactory) {
        this.privateGbmEglPlatformFactory = privateGbmEglPlatformFactory;
        this.libdrm = libdrm;
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
            gbmEglConnectors[i] = createGbmEglConnector(gbmDevice,
                                                        drmConnector);
        }

        this.privateGbmEglPlatformFactory.create(gbmDevice,
                                                 eglDisplay,
                                                 eglContext,
                                                 eglExtensions,
                                                 gbmEglConnectors);

    }

    private GbmEglConnector createGbmEglConnector(final long gbmDevice,
                                                  final DrmConnector drmConnector) {

        final long gbmSurface = this.libgbm.gbm_surface_create(gbmDevice,
                                                               drmConnector.getMode()
                                                                           .hdisplay(),
                                                               drmConnector.getMode()
                                                                           .vdisplay(),
                                                               GBM_FORMAT_XRGB8888,
                                                               GBM_BO_USE_SCANOUT | GBM_BO_USE_RENDERING);

        final long gbmBo = this.libgbm.gbm_surface_lock_front_buffer(gbmSurface);

        final GbmEglConnector gbmEglConnector = this.eglGbmConnectorFactory.create(this.drmPlatform.getDrmFd(),
                                                                                   gbmBo,
                                                                                   gbmSurface,
                                                                                   drmConnector);
        final int fbId = gbmEglConnector.getFbId(gbmBo);
        this.libdrm.drmModeSetCrtc(this.drmPlatform.getDrmFd(),
                                   drmConnector.getCrtcId(),
                                   fbId,
                                   0,
                                   0,
                                   Pointer.nref(drmConnector.getDrmModeConnector()
                                                            .connector_id()).address,
                                   1,
                                   Pointer.ref(drmConnector.getMode()).address);

        return gbmEglConnector;
    }
}
