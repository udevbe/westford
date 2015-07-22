package org.westmalle.wayland.core.events;

import com.google.auto.value.AutoValue;

import org.freedesktop.wayland.shared.WlKeyboardKeyState;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

@AutoValue
public abstract class Key {
    public static Key create(final int time,
           @Nonnegative final int key,
           @Nonnull final WlKeyboardKeyState wlKeyboardKeyState) {
        return new AutoValue_Key(time,
                                 key,
                                 wlKeyboardKeyState);
    }

    public abstract int getTime();

    public abstract int getKey();

    public abstract WlKeyboardKeyState getKeyState();
}
