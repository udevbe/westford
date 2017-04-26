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

import com.google.auto.factory.AutoFactory
import org.westford.nativ.libxcb.xcb_screen_t

@AutoFactory(allowSubclasses = true, className = "X11OutputFactory")
class X11Output internal constructor(val xWindow: Int,
                                     val x: Int,
                                     val y: Int,
                                     val width: Int,
                                     val height: Int,
                                     val name: String,
                                     val screen: xcb_screen_t)
