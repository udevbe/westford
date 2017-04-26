package org.westford.compositor.drm.egl

import com.google.auto.factory.AutoFactory
import com.google.auto.factory.Provided
import org.westford.nativ.libgbm.Libgbm

@AutoFactory(allowSubclasses = true, className = "PrivateGbmBoServerFactory")
class GbmBoServer internal constructor(@param:Provided private val libgbm: Libgbm,
                                       private val gbmSurface: Long,
                                       override val gbmBo: Long) : GbmBo {

    override fun close() {
        this.libgbm.gbm_surface_release_buffer(this.gbmSurface,
                this.gbmBo)
    }
}
