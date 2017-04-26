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

@AutoFactory(className = "PrivateX11PlatformFactory", allowSubclasses = true)
class X11Platform internal constructor(val renderOutputs: List<X11Output>,
                                       val x11EventBus: X11EventBus,
                                       val xcbConnection: Long,
                                       private val xDisplay: Long,
                                       val x11Atoms: Map<String, Int>) {

    fun getxDisplay(): Long {
        return this.xDisplay
    }
}
