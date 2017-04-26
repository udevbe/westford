/*
 * Westford Wayland Compositor.
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
package org.westford.compositor.x11

import org.westford.compositor.core.Xkb
import org.westford.compositor.core.XkbFactory
import org.westford.nativ.libxkbcommon.Libxkbcommon
import org.westford.nativ.libxkbcommonx11.Libxkbcommonx11
import javax.inject.Inject

import org.westford.nativ.libxkbcommon.Libxkbcommon.Companion.XKB_CONTEXT_NO_FLAGS
import org.westford.nativ.libxkbcommon.Libxkbcommon.Companion.XKB_KEYMAP_COMPILE_NO_FLAGS

class X11XkbFactory @Inject
internal constructor(private val libxkbcommon: Libxkbcommon,
                     private val xkbFactory: XkbFactory,
                     private val libxkbcommonx11: Libxkbcommonx11) {

    fun create(xcbConnection: Long): Xkb {

        val xkbContext = this.libxkbcommon.xkb_context_new(XKB_CONTEXT_NO_FLAGS)
        if (xkbContext == 0L) {
            throw RuntimeException("Got an error while trying to create xkb context. " +
                    "Unfortunately the docs of the xkb library do not specify how to get more information " +
                    "about the error, so you'll have to do it with this lousy exception.")
        }

        val device_id = this.libxkbcommonx11.xkb_x11_get_core_keyboard_device_id(xcbConnection)
        if (device_id == -1) {
            throw RuntimeException("Got an error while trying to fetch keyboard device id from X11 backend. " +
                    "Unfortunately the docs of the xkb library do not specify how to get more information " +
                    "about the error, so you'll have to do it with this lousy exception.")
        }

        val keymap = this.libxkbcommonx11.xkb_x11_keymap_new_from_device(xkbContext,
                xcbConnection,
                device_id,
                XKB_KEYMAP_COMPILE_NO_FLAGS)
        if (keymap == 0L) {
            throw RuntimeException("Got an error while trying to get x11 keymap. " +
                    "Unfortunately the docs of the xkb library do not specify how to get more information " +
                    "about the error, so you'll have to do it with this lousy exception.")
        }

        val state = this.libxkbcommonx11.xkb_x11_state_new_from_device(keymap,
                xcbConnection,
                device_id)

        return this.xkbFactory.create(xkbContext,
                state,
                keymap)
    }
}
