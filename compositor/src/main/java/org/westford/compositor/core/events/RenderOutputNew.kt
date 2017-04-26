package org.westford.compositor.core.events

import com.google.auto.value.AutoValue
import org.westford.compositor.protocol.WlOutput

@AutoValue
abstract class RenderOutputNew {

    abstract val wlOutput: WlOutput

    companion object {
        fun create(wlOutput: WlOutput): RenderOutputNew {
            return AutoValue_RenderOutputNew(wlOutput)
        }
    }
}
