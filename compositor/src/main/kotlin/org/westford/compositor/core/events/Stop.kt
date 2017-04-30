package org.westford.compositor.core.events

import com.google.auto.value.AutoValue

@AutoValue abstract class Stop {
    companion object {
        fun create(): Stop {
            return AutoValue_Stop()
        }
    }
}
