package org.westmalle.compositor.core.events;


import com.google.auto.value.AutoValue;
import org.westmalle.compositor.core.events.AutoValue_OutputTransform;

@AutoValue
public abstract class OutputTransform {

    public static OutputTransform create() {
        return new AutoValue_OutputTransform();
    }
}
