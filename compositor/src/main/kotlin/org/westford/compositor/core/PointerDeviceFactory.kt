package org.westford.compositor.core

import javax.inject.Inject

class PointerDeviceFactory @Inject internal constructor(private val privatePointerDeviceFactory: PrivatePointerDeviceFactory,
                                                        private val renderPlatform: RenderPlatform,
                                                        private val finiteRegionFactory: FiniteRegionFactory) {

    fun create(): PointerDevice {
        var outputsRegion = this.finiteRegionFactory.create()

        this.renderPlatform.wlOutputs.forEach {
            outputsRegion += it.output.region
        }

        return this.privatePointerDeviceFactory.create(outputsRegion)
    }
}
