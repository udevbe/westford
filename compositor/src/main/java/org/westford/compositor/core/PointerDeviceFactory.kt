package org.westford.compositor.core


import javax.inject.Inject

class PointerDeviceFactory @Inject
internal constructor(private val privatePointerDeviceFactory: PrivatePointerDeviceFactory,
                     private val renderPlatform: RenderPlatform,
                     private val finiteRegionFactory: FiniteRegionFactory) {

    fun create(): PointerDevice {
        val outputsRegion = this.finiteRegionFactory.create()
        val pointerDevice = this.privatePointerDeviceFactory.create(outputsRegion)

        this.renderPlatform.wlOutputs
                .forEach { wlOutput ->
                    outputsRegion.add(wlOutput.output
                            .region)
                }
        return pointerDevice
    }
}
