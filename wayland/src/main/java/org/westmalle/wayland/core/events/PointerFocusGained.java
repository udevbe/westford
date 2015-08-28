package org.westmalle.wayland.core.events;

import com.google.auto.value.AutoValue;
import org.freedesktop.wayland.server.WlPointerResource;

import java.util.Set;

@AutoValue
public abstract class PointerFocusGained {

    public static PointerFocusGained create(final Set<WlPointerResource> wlPointerResources) {
        return new AutoValue_PointerFocusGained(wlPointerResources);
    }

    public abstract Set<WlPointerResource> getWlPointerResources();
}
