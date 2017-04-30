package org.westford.compositor.core.events

import com.google.auto.value.AutoValue

@AutoValue abstract class Activate {
    fun create(): Activate {
        return AutoValue_Activate()
    }
}
