package org.westford.compositor.core.events;

import com.google.auto.value.AutoValue;
import org.westford.compositor.protocol.WlOutput;

import javax.annotation.Nonnull;

@AutoValue
public abstract class RenderOutputNew {
    public static RenderOutputNew create(@Nonnull final WlOutput wlOutput) {
        return new AutoValue_RenderOutputNew(wlOutput);
    }

    @Nonnull
    public abstract WlOutput getWlOutput();
}
