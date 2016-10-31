package org.westford.compositor.core.events;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Deactivate {
    public static Deactivate create() {
        return new AutoValue_Deactivate();
    }
}
