package org.westford.compositor.protocol


import org.westford.compositor.core.PointerDevice
import org.westford.compositor.core.PointerDeviceFactory
import org.westford.compositor.core.RenderPlatform
import javax.inject.Inject

class WlPointerFactory @Inject
internal constructor(private val renderPlatform: RenderPlatform,
                     private val pointerDeviceFactory: PointerDeviceFactory,
                     private val privateWlPointerFactory: PrivateWlPointerFactory) {

    fun create(): WlPointer {
        val pointerDevice = this.pointerDeviceFactory.create()
        val wlPointer = this.privateWlPointerFactory.create(pointerDevice)

        this.renderPlatform.renderOutputNewSignal
                .connect({ event ->
                    pointerDevice.clampRegion
                            .add(event.getWlOutput()
                                    .output
                                    .getRegion())
                })
        //TODO unit test
        this.renderPlatform.renderOutputDestroyedSignal
                .connect({ event ->
                    pointerDevice.clampRegion
                            .remove(event.getWlOutput()
                                    .output
                                    .getRegion())
                    pointerDevice.clamp(wlPointer.getResources(),
                            pointerDevice.position)
                })

        return wlPointer
    }
}
