package org.westmalle.wayland.core.events;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class RenderBegin {

    public static RenderBegin create(final long time) {
        return new AutoValue_RenderBegin(time);
    }

    public abstract long getTime();
}
