package org.westmalle.wayland.core.events;


import com.google.auto.value.AutoValue;

@AutoValue
public abstract class OutputTransform {

    public static OutputTransform create() {
        return new AutoValue_OutputTransform();
    }
}
