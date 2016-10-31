package org.westmalle.compositor.core.events;

import com.google.auto.value.AutoValue;
import org.westmalle.compositor.core.RenderOutput;
import org.westmalle.compositor.core.events.AutoValue_RenderOutputDestroyed;

import javax.annotation.Nonnull;

@AutoValue
public abstract class RenderOutputDestroyed {
    public static RenderOutputDestroyed create(@Nonnull final RenderOutput renderOutput) {
        return new AutoValue_RenderOutputDestroyed(renderOutput);
    }

    @Nonnull
    public abstract RenderOutput getRenderOutput();
}
