package org.westmalle.wayland.core;


import com.google.auto.value.AutoValue;
import org.freedesktop.wayland.shared.WlKeyboardKeymapFormat;

import javax.annotation.Nonnull;

@AutoValue
public abstract class Keymap {

    public static Keymap create(@Nonnull final WlKeyboardKeymapFormat format,
                                @Nonnull final String map) {
        return new AutoValue_Keymap(format,
                                    map);
    }

    public abstract WlKeyboardKeymapFormat getFormat();


    public abstract String getMap();
}
