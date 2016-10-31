package org.westmalle.compositor.core.events;

import com.google.auto.value.AutoValue;
import org.westmalle.compositor.core.events.AutoValue_Start;

@AutoValue
public abstract class Start {
    public static Start create() {
        return new AutoValue_Start();
    }
}
