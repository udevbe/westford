package org.westmalle.wayland.core;


import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import com.sun.jna.Pointer;
import org.westmalle.wayland.nativ.libxkbcommon.Libxkbcommon;

import javax.annotation.Nonnull;

import static org.westmalle.wayland.nativ.libxkbcommon.Libxkbcommon.XKB_KEYMAP_FORMAT_TEXT_V1;

@AutoFactory
public class Xkb {

    @Nonnull
    private final Libxkbcommon libxkbcommon;

    @Nonnull
    private final Pointer xkbContext;
    @Nonnull
    private final Pointer xkbState;
    @Nonnull
    private final Pointer xkbKeymap;

    Xkb(@Provided @Nonnull final Libxkbcommon libxkbcommon,
        @Nonnull final Pointer xkbContext,
        @Nonnull final Pointer xkbState,
        @Nonnull final Pointer xkbKeymap) {
        this.libxkbcommon = libxkbcommon;
        this.xkbContext = xkbContext;
        this.xkbState = xkbState;
        this.xkbKeymap = xkbKeymap;
    }

    public String getKeymapString() {
        final Pointer keymapStringPointer = this.libxkbcommon.xkb_keymap_get_as_string(this.xkbKeymap,
                                                                                       XKB_KEYMAP_FORMAT_TEXT_V1);
        if (keymapStringPointer == null) {
            throw new RuntimeException("Got an error while trying to get keymap as string. " +
                                       "Unfortunately the docs of the xkb library do not specify how we to get more information " +
                                       "about the error, so you'll have to do it with this lousy exception.");
        }

        return keymapStringPointer.getString(0);
    }
}
