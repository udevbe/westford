package org.westmalle.wayland.core.events;

import com.google.auto.value.AutoValue;
import org.freedesktop.wayland.server.WlSurfaceResource;

import java.util.Optional;

@AutoValue
public abstract class PointerGrab {
    public static PointerGrab create(final Optional<WlSurfaceResource> wlSurfaceResource) {
        return new AutoValue_PointerGrab(wlSurfaceResource);
    }

    public abstract Optional<WlSurfaceResource> getWlSurfaceResource();
}
