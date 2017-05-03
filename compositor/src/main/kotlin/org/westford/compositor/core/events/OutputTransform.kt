package org.westford.compositor.core.events

import com.google.auto.value.AutoValue

@AutoValue abstract class OutputTransform {

    companion object {
        fun create(): OutputTransform = AutoValue_OutputTransform()
    }
}
