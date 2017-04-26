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
import org.freedesktop.wayland.server.WlSurfaceResource
import org.westford.compositor.protocol.WlSurface
import java.util.Optional

@AutoFactory(className = "CursorFactory", allowSubclasses = true)
class Cursor internal constructor(var wlSurfaceResource: WlSurfaceResource,
                                  var hotspot: Point) {
    var isHidden: Boolean = false
        private set

    fun updatePosition(pointerPosition: Point) {
        val wlSurface = this.wlSurfaceResource.implementation as WlSurface
        val surface = wlSurface.surface
        surface.views
                .forEach { surfaceView -> surfaceView.setPosition(pointerPosition.subtract(hotspot)) }
    }

    fun hide() {
        val wlSurface = this.wlSurfaceResource.implementation as WlSurface
        val surface = wlSurface.surface

        surface.state = surface.state
                .toBuilder()
                .buffer(Optional.empty<WlBufferResource>())
                .build()

        this.isHidden = true
    }

    fun show() {
        this.isHidden = false
    }
}
