package org.westmalle.compositor.core.events;

import com.google.auto.value.AutoValue;
import org.westmalle.compositor.core.events.AutoValue_Activate;

@AutoValue
public abstract class Activate {
    public static Activate create() {
        return new AutoValue_Activate();
    }
}
