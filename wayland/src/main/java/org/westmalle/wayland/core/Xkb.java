/*
 * Westmalle Wayland Compositor.
 * Copyright (C) 2016  Erik De Rijcke
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.westmalle.wayland.core;

import org.freedesktop.jaccall.Pointer;
import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import org.westmalle.wayland.nativ.libxkbcommon.Libxkbcommon;

import javax.annotation.Nonnull;

import static org.westmalle.wayland.nativ.libxkbcommon.Libxkbcommon.XKB_KEYMAP_FORMAT_TEXT_V1;

@AutoFactory(className = "XkbFactory",
             allowSubclasses = true)
public class Xkb {

    @Nonnull
    private final Libxkbcommon libxkbcommon;

    private final long xkbContext;
    private final long xkbState;
    private final long xkbKeymap;

    Xkb(@Provided @Nonnull final Libxkbcommon libxkbcommon,
        final long xkbContext,
        final long xkbState,
        final long xkbKeymap) {
        this.libxkbcommon = libxkbcommon;
        this.xkbContext = xkbContext;
        this.xkbState = xkbState;
        this.xkbKeymap = xkbKeymap;
    }

    public String getKeymapString() {
        try (final Pointer<String> keymapStringPointer = Pointer.wrap(String.class,
                                                                      this.libxkbcommon.xkb_keymap_get_as_string(this.xkbKeymap,
                                                                                                                 XKB_KEYMAP_FORMAT_TEXT_V1))) {
            if (keymapStringPointer.address == 0L) {
                throw new RuntimeException("Got an error while trying to get keymap as string. " +
                                           "Unfortunately the docs of the xkb library do not specify how we to get more information " +
                                           "about the error, so you'll have to do it with this lousy exception.");
            }
            return keymapStringPointer.dref();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        this.libxkbcommon.xkb_context_unref(getContext());
        this.libxkbcommon.xkb_keymap_unref(getKeymap());
        this.libxkbcommon.xkb_state_unref(getState());
        super.finalize();
    }

    public long getContext() {
        return this.xkbContext;
    }

    public long getKeymap() {
        return this.xkbKeymap;
    }

    public long getState() {
        return this.xkbState;
    }
}
