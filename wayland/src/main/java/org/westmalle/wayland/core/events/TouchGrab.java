package org.westmalle.wayland.core.events;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class TouchGrab {
    public static TouchGrab create(){
        return new AutoValue_TouchGrab();
    }
}
