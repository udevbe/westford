package org.westford.compositor.drm.egl

import com.google.auto.factory.AutoFactory
import com.google.auto.factory.Provided
import org.westford.nativ.libgbm.Libgbm

@AutoFactory(allowSubclasses = true,
             className = "PrivateGbmBoClientFactory") class GbmBoClient(@param:Provided private val libgbm: Libgbm,
                                                                        override val gbmBo: Long) : GbmBo {

    override fun close() = this.libgbm.gbm_bo_destroy(this.gbmBo)
}
