package org.westmalle.wayland.core.events;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class KeyboardFocusChanged {

    public static KeyboardFocusChanged create() {
        return new AutoValue_KeyboardFocusChanged();
    }
}
