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
package org.westmalle.wayland.input;


import org.freedesktop.jaccall.Pointer;
import org.westmalle.nativ.libxkbcommon.Libxkbcommon;
import org.westmalle.nativ.libxkbcommon.xkb_rule_names;
import org.westmalle.wayland.core.Xkb;
import org.westmalle.wayland.core.XkbFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import static org.westmalle.nativ.libxkbcommon.Libxkbcommon.XKB_CONTEXT_NO_FLAGS;

public class LibinputXkbFactory {

    @Nonnull
    private final Libxkbcommon libxkbcommon;
    @Nonnull
    private final XkbFactory   xkbFactory;

    @Inject
    LibinputXkbFactory(@Nonnull final Libxkbcommon libxkbcommon,
                       @Nonnull final XkbFactory xkbFactory) {
        this.libxkbcommon = libxkbcommon;
        this.xkbFactory = xkbFactory;
    }

    public Xkb create(@Nonnull final String rule,
                      @Nonnull final String model,
                      @Nonnull final String layout,
                      @Nonnull final String variant,
                      @Nonnull final String options) {

        final long xkbContext = this.libxkbcommon.xkb_context_new(XKB_CONTEXT_NO_FLAGS);
        if (xkbContext == 0L) {
            throw new RuntimeException("Got an error while trying to create xkb context. " +
                                       "Unfortunately the docs of the xkb library do not specify how to get more information " +
                                       "about the error, so you'll have to do it with this lousy exception.");
        }

        final xkb_rule_names names = new xkb_rule_names();
        names.rules(Pointer.nref(rule));
        names.model(Pointer.nref(model));
        names.layout(Pointer.nref(layout));
        names.variant(Pointer.nref(variant));
        names.options(Pointer.nref(options));

        final long keymap = this.libxkbcommon.xkb_keymap_new_from_names(xkbContext,
                                                                        Pointer.ref(names).address,
                                                                        Libxkbcommon.XKB_KEYMAP_COMPILE_NO_FLAGS);

        if (keymap == 0L) {
            throw new RuntimeException("Got an error while trying to get x11 keymap. " +
                                       "Unfortunately the docs of the xkb library do not specify how to get more information " +
                                       "about the error, so you'll have to do it with this lousy exception.");
        }

        final long state = this.libxkbcommon.xkb_state_new(keymap);

        return this.xkbFactory.create(xkbContext,
                                      state,
                                      keymap);
    }
}
