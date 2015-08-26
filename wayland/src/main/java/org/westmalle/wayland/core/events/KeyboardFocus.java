package org.westmalle.wayland.core.events;

import com.google.auto.value.AutoValue;
import org.freedesktop.wayland.server.WlSurfaceResource;

import java.util.Optional;

@AutoValue
public abstract class KeyboardFocus {

    public static KeyboardFocus create(final Optional<WlSurfaceResource> wlSurfaceResource) {
        return new AutoValue_KeyboardFocus(wlSurfaceResource);
    }

    public abstract Optional<WlSurfaceResource> getWlSurfaceResource();
}
