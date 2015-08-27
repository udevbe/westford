package org.westmalle.wayland.core.events;

import com.google.auto.value.AutoValue;
import org.freedesktop.wayland.server.WlKeyboardResource;

import javax.annotation.Nonnull;
import java.util.Set;

@AutoValue
public abstract class KeyboardFocusGained {
    public static KeyboardFocusGained create(@Nonnull Set<WlKeyboardResource> wlKeyboardResources) {
        return new AutoValue_KeyboardFocusGained(wlKeyboardResources);
    }

    public abstract Set<WlKeyboardResource> getWlKeyboardResources();
}
