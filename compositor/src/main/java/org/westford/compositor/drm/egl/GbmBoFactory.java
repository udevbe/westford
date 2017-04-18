package org.westford.compositor.drm.egl;


import org.freedesktop.wayland.server.WlBufferResource;
import org.westford.nativ.libgbm.Libgbm;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import static org.westford.nativ.libgbm.Libgbm.GBM_BO_IMPORT_WL_BUFFER;
import static org.westford.nativ.libgbm.Libgbm.GBM_BO_USE_SCANOUT;

public class GbmBoFactory {

    @Nonnull
    private final Libgbm                    libgbm;
    @Nonnull
    private final PrivateGbmBoClientFactory privateGbmBoClientFactory;
    @Nonnull
    private final PrivateGbmBoServerFactory privateGbmBoServerFactory;

    @Inject
    GbmBoFactory(@Nonnull final Libgbm libgbm,
                 @Nonnull final PrivateGbmBoClientFactory privateGbmBoClientFactory,
                 @Nonnull final PrivateGbmBoServerFactory privateGbmBoServerFactory) {
        this.libgbm = libgbm;
        this.privateGbmBoClientFactory = privateGbmBoClientFactory;
        this.privateGbmBoServerFactory = privateGbmBoServerFactory;
    }

    /**
     * Create a gbm object from a server side (local) gbm surface.
     *
     * @param gbmSurface
     *
     * @return
     */
    public GbmBo create(long gbmSurface) {
        final long gbmBo = this.libgbm.gbm_surface_lock_front_buffer(gbmSurface);
        return this.privateGbmBoServerFactory.create(gbmSurface,
                                                     gbmBo);
    }

    /**
     * Create a gbm object from a client (non local) wayland buffer resource.
     *
     * @param gbmDevice
     * @param wlBufferResource
     *
     * @return
     */
    public GbmBo create(final long gbmDevice,
                        final WlBufferResource wlBufferResource) {
        final long gbmBo = this.libgbm.gbm_bo_import(gbmDevice,
                                                     GBM_BO_IMPORT_WL_BUFFER,
                                                     wlBufferResource.pointer,
                                                     GBM_BO_USE_SCANOUT);
        return this.privateGbmBoClientFactory.create(gbmBo);
    }
}
