package org.westford.compositor.core.events

import com.google.auto.value.AutoValue
import org.westford.compositor.protocol.WlOutput

@AutoValue abstract class RenderOutputDestroyed {

    abstract val wlOutput: WlOutput

    companion object {
        fun create(wlOutput: WlOutput): RenderOutputDestroyed {
            return AutoValue_RenderOutputDestroyed(wlOutput)
        }
    }
}
