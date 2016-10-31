package org.westford.compositor.core.events;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Activate {
    public static Activate create() {
        return new AutoValue_Activate();
    }
}
