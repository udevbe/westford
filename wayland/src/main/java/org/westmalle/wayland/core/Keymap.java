package org.westmalle.wayland.core;


import com.google.auto.value.AutoValue;
import org.freedesktop.wayland.shared.WlKeyboardKeymapFormat;

@AutoValue
public abstract class Keymap {

    public static Keymap create(WlKeyboardKeymapFormat format,
                                String map) {
        return new AutoValue_Keymap(format,
                                    map);
    }

    public abstract WlKeyboardKeymapFormat getFormat();

    public abstract String getMap();
}
