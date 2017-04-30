package org.westford.compositor.core.events

import com.google.auto.value.AutoValue

@AutoValue abstract class Start {
    companion object {
        fun create(): Start = AutoValue_Start()
    }
}
