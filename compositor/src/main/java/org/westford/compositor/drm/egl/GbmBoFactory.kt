package org.westford.compositor.drm.egl


import org.freedesktop.wayland.server.WlBufferResource
import org.westford.nativ.libgbm.Libgbm
import javax.inject.Inject

import org.westford.nativ.libgbm.Libgbm.Companion.GBM_BO_IMPORT_WL_BUFFER
import org.westford.nativ.libgbm.Libgbm.Companion.GBM_BO_USE_SCANOUT

class GbmBoFactory @Inject
internal constructor(private val libgbm: Libgbm,
                     private val privateGbmBoClientFactory: PrivateGbmBoClientFactory,
                     private val privateGbmBoServerFactory: PrivateGbmBoServerFactory) {

    /**
     * Create a gbm object from a server side (local) gbm surface.

     * @param gbmSurface
     * *
     * *
     * @return
     */
    fun create(gbmSurface: Long): GbmBo {
        val gbmBo = this.libgbm.gbm_surface_lock_front_buffer(gbmSurface)
        return this.privateGbmBoServerFactory.create(gbmSurface,
                gbmBo)
    }

    /**
     * Create a gbm object from a client (non local) wayland buffer resource.

     * @param gbmDevice
     * *
     * @param wlBufferResource
     * *
     * *
     * @return
     */
    fun create(gbmDevice: Long,
               wlBufferResource: WlBufferResource): GbmBo {
        val gbmBo = this.libgbm.gbm_bo_import(gbmDevice,
                GBM_BO_IMPORT_WL_BUFFER,
                wlBufferResource.pointer,
                GBM_BO_USE_SCANOUT)
        return this.privateGbmBoClientFactory.create(gbmBo)
    }
}
