package org.westmalle.wayland.core.events;

import com.google.auto.value.AutoValue;

@AutoValue
public class Start {
    public static Start create() {
        return new AutoValue_Start();
    }
}
