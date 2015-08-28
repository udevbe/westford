package org.westmalle.wayland.core.events;

import com.google.auto.value.AutoValue;
import org.freedesktop.wayland.server.WlPointerResource;

import java.util.Set;

@AutoValue
public abstract class PointerFocusLost {
    public static PointerFocusLost create(final Set<WlPointerResource> clientPointerResources) {
        return new AutoValue_PointerFocusLost(clientPointerResources);
    }

    public abstract Set<WlPointerResource> getWlPointerResources();
}
