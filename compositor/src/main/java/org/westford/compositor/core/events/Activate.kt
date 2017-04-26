package org.westford.compositor.core.events

import com.google.auto.value.AutoValue

@AutoValue
object Activate {
    fun create(): Activate {
        return AutoValue_Activate()
    }
}
