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
package org.westford.nativ.libX11xcb

import org.freedesktop.jaccall.Lib
import org.freedesktop.jaccall.Ptr

import javax.inject.Singleton

@Singleton
@Lib(value = "X11-xcb", version = 1)
class LibX11xcb {

    @Ptr
    external fun XGetXCBConnection(@Ptr dpy: Long): Long

    external fun XSetEventQueueOwner(@Ptr dpy: Long,
                                     owner: Int)

    companion object {
        val XCBOwnsEventQueue = 1
    }
}
