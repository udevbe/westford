package org.westmalle.wayland.core.events;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class TouchMotion {
    public static TouchMotion create(){
        return new AutoValue_TouchMotion();
    }
}
