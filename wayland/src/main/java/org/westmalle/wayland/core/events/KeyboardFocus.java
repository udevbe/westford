package org.westmalle.wayland.core.events;

import com.google.auto.value.AutoValue;
import org.freedesktop.wayland.server.WlSurfaceResource;

import javax.annotation.Nonnull;
import java.util.Optional;

@AutoValue
public abstract class KeyboardFocus {

    public static KeyboardFocus create(@Nonnull final Optional<WlSurfaceResource> wlSurfaceResource) {
        return new AutoValue_KeyboardFocus(wlSurfaceResource);
    }

    public abstract Optional<WlSurfaceResource> getWlSurfaceResource();
}
