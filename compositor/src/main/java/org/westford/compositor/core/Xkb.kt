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
package org.westford.compositor.core

import com.google.auto.factory.AutoFactory
import com.google.auto.factory.Provided
import org.freedesktop.jaccall.Pointer
import org.westford.nativ.libxkbcommon.Libxkbcommon

import org.westford.nativ.libxkbcommon.Libxkbcommon.Companion.XKB_KEYMAP_FORMAT_TEXT_V1

@AutoFactory(className = "XkbFactory",
             allowSubclasses = true) class Xkb(@param:Provided private val libxkbcommon: Libxkbcommon,
                                               val context: Long,
                                               val state: Long,
                                               val keymap: Long) {

    val keymapString: String
        get() {
            Pointer.wrap<String>(String::class.java,
                                 this.libxkbcommon.xkb_keymap_get_as_string(this.keymap,
                                                                            XKB_KEYMAP_FORMAT_TEXT_V1)).use {
                if (it.address == 0L) {
                    throw RuntimeException("Got an error while trying to get keymap as string.\nUnfortunately the docs of the xkb library do not specify how we to get more information about the error, so you'll have to do it with this lousy exception.")
                }
                return it.dref()
            }
        }

    fun finalize() {
        this.libxkbcommon.xkb_context_unref(context)
        this.libxkbcommon.xkb_keymap_unref(keymap)
        this.libxkbcommon.xkb_state_unref(state)
    }
}
