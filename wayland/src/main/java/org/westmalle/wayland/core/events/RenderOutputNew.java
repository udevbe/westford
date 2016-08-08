package org.westmalle.wayland.core.events;

import com.google.auto.value.AutoValue;
import org.westmalle.wayland.core.RenderOutput;

import javax.annotation.Nonnull;

@AutoValue
public abstract class RenderOutputNew {
    public static RenderOutputNew create(@Nonnull final RenderOutput renderOutput) {
        return new AutoValue_RenderOutputNew(renderOutput);
    }

    @Nonnull
    public abstract RenderOutput getRenderOutput();
}
