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
package org.westford.compositor.protocol

import com.google.auto.factory.AutoFactory
import org.freedesktop.wayland.server.Client
import org.freedesktop.wayland.server.Display
import org.freedesktop.wayland.server.WlPointerRequestsV5
import org.freedesktop.wayland.server.WlPointerResource
import org.freedesktop.wayland.server.WlSurfaceResource
import org.freedesktop.wayland.shared.WlPointerError
import org.westford.compositor.core.PointerDevice
import java.util.*
import javax.annotation.Nonnegative

@AutoFactory(allowSubclasses = true,
             className = "PrivateWlPointerFactory") class WlPointer(val pointerDevice: PointerDevice) : WlPointerRequestsV5, ProtocolObject<WlPointerResource> {

    override val resources: MutableSet<WlPointerResource> = Collections.newSetFromMap(WeakHashMap<WlPointerResource, Boolean>())

    override fun setCursor(wlPointerResource: WlPointerResource,
                           serial: Int,
                           wlSurfaceResource: WlSurfaceResource?,
                           hotspotX: Int,
                           hotspotY: Int) {

        if (wlSurfaceResource == null) {
            pointerDevice.removeCursor(wlPointerResource,
                                       serial)
            return
        }

        val wlSurface = wlSurfaceResource.implementation as WlSurface
        val surface = wlSurface.surface

        val role = surface.role
        if (role != null && role != this.pointerDevice) {
            wlPointerResource.client.getObject(Display.OBJECT_ID).postError(WlPointerError.ROLE.value,
                                                                            String.format("Desired cursor surface already has another role (%s)",
                                                                                          role.javaClass.simpleName))
            return
        }

        surface.role = this.pointerDevice
        this.pointerDevice.setCursor(wlPointerResource,
                                     serial,
                                     wlSurfaceResource,
                                     hotspotX,
                                     hotspotY)
    }

    override fun release(resource: WlPointerResource) = resource.destroy()

    override fun create(client: Client,
                        @Nonnegative version: Int,
                        id: Int): WlPointerResource = WlPointerResource(client,
                                                                        version,
                                                                        id,
                                                                        this)
}
