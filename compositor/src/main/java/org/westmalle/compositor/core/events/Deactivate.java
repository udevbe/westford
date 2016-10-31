package org.westmalle.compositor.core.events;

import com.google.auto.value.AutoValue;
import org.westmalle.compositor.core.events.AutoValue_Deactivate;

@AutoValue
public abstract class Deactivate {
    public static Deactivate create() {
        return new AutoValue_Deactivate();
    }
}
