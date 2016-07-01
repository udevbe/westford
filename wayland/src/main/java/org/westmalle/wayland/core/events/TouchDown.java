package org.westmalle.wayland.core.events;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class TouchDown {
    public static TouchDown create() {
        return new AutoValue_TouchDown();
    }
}
