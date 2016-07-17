package org.westmalle.wayland.core.events;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class RenderEndAfterSwap {

    public static RenderEndAfterSwap create(final long time){
        return new AutoValue_RenderEndAfterSwap(time);
    }

    public abstract long getTime();
}
