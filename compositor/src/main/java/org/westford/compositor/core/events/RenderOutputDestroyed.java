package org.westford.compositor.core.events;

import com.google.auto.value.AutoValue;
import org.westford.compositor.core.RenderOutput;

import javax.annotation.Nonnull;

@AutoValue
public abstract class RenderOutputDestroyed {
    public static RenderOutputDestroyed create(@Nonnull final RenderOutput renderOutput) {
        return new AutoValue_RenderOutputDestroyed(renderOutput);
    }

    @Nonnull
    public abstract RenderOutput getRenderOutput();
}
