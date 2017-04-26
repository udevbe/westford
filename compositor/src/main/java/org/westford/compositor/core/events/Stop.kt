package org.westford.compositor.core.events

import com.google.auto.value.AutoValue

@AutoValue
object Stop {
    fun create(): Stop {
        return AutoValue_Stop()
    }
}
