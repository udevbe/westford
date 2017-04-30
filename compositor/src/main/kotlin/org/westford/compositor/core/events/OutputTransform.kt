package org.westford.compositor.core.events

import com.google.auto.value.AutoValue

@AutoValue abstract class OutputTransform {

    fun create(): OutputTransform {
        return AutoValue_OutputTransform()
    }
}
