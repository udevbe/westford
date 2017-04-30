package org.westford.compositor.core.events

import com.google.auto.value.AutoValue

@AutoValue abstract class Deactivate {
    fun create(): Deactivate {
        return AutoValue_Deactivate()
    }
}
