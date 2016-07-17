package org.westmalle.wayland.core.events;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class RenderEndBeforeSwap {

    public static RenderEndBeforeSwap create(final long time){
        return new AutoValue_RenderEndBeforeSwap(time);
    }

    public abstract long getTime();
}
