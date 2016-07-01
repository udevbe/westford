package org.westmalle.wayland.core.events;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class TouchUp {
    public static TouchUp create() {
        return new AutoValue_TouchUp();
    }
}
