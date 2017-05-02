package org.westford.compositor.core.events

import com.google.auto.value.AutoValue

@AutoValue abstract class Deactivate {
    companion object {
        fun create(): Deactivate {
            return AutoValue_Deactivate()
        }
    }
}
