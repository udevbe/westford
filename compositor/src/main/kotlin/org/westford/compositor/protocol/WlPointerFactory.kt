package org.westford.compositor.protocol

import org.westford.compositor.core.PointerDeviceFactory
import org.westford.compositor.core.RenderPlatform
import javax.inject.Inject

class WlPointerFactory @Inject internal constructor(private val renderPlatform: RenderPlatform,
                                                    private val pointerDeviceFactory: PointerDeviceFactory,
                                                    private val privateWlPointerFactory: PrivateWlPointerFactory) {

    fun create(): WlPointer {
        val pointerDevice = this.pointerDeviceFactory.create()
        val wlPointer = this.privateWlPointerFactory.create(pointerDevice)

        this.renderPlatform.renderOutputNewSignal.connect {
            pointerDevice.clampRegion.add(it.wlOutput.output.region)
        }
        //TODO unit test
        this.renderPlatform.renderOutputDestroyedSignal.connect {
            pointerDevice.clampRegion.remove(it.wlOutput.output.region)
            pointerDevice.clamp(wlPointer.getResources(),
                                pointerDevice.position)
        }

        return wlPointer
    }
}
