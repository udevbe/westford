package org.westford.compositor.core.events;

import com.google.auto.value.AutoValue;
import org.westford.compositor.core.RenderOutput;
import org.westford.compositor.protocol.WlOutput;

import javax.annotation.Nonnull;

@AutoValue
public abstract class RenderOutputDestroyed {
    public static RenderOutputDestroyed create(@Nonnull final WlOutput wlOutput) {
        return new AutoValue_RenderOutputDestroyed(wlOutput);
    }

    @Nonnull
    public abstract WlOutput getWlOutput();
}
