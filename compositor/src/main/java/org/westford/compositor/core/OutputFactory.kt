package org.westford.compositor.core

import org.freedesktop.wayland.server.WlOutputResource
import javax.inject.Inject

class OutputFactory @Inject internal constructor(private val privateOutputFactory: PrivateOutputFactory) {

    fun create(renderOutput: RenderOutput,
               name: String,
               outputGeometry: OutputGeometry,
               outputMode: OutputMode): Output {
        val output = this.privateOutputFactory.create(renderOutput,
                                                      name)
        output.update(emptySet<WlOutputResource>(),
                      outputGeometry)
        output.update(emptySet<WlOutputResource>(),
                      outputMode)
        return output
    }
}
