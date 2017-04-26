package org.westford.compositor.core.events

import com.google.auto.value.AutoValue

@AutoValue
object Start {
    fun create(): Start {
        return AutoValue_Start()
    }
}
