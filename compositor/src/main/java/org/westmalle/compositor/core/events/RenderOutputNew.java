package org.westmalle.compositor.core.events;

import com.google.auto.value.AutoValue;
import org.westmalle.compositor.core.RenderOutput;
import org.westmalle.compositor.core.events.AutoValue_RenderOutputNew;

import javax.annotation.Nonnull;

@AutoValue
public abstract class RenderOutputNew {
    public static RenderOutputNew create(@Nonnull final RenderOutput renderOutput) {
        return new AutoValue_RenderOutputNew(renderOutput);
    }

    @Nonnull
    public abstract RenderOutput getRenderOutput();
}
