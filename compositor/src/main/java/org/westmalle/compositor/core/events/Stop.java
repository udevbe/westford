package org.westmalle.compositor.core.events;

import com.google.auto.value.AutoValue;
import org.westmalle.compositor.core.events.AutoValue_Stop;

@AutoValue
public abstract class Stop {
    public static Stop create() {
        return new AutoValue_Stop();
    }
}
