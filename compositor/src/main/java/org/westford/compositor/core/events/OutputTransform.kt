package org.westford.compositor.core.events


import com.google.auto.value.AutoValue

@AutoValue
object OutputTransform {

    fun create(): OutputTransform {
        return AutoValue_OutputTransform()
    }
}
