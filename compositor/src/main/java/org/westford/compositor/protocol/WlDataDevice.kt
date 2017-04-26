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

import org.freedesktop.wayland.server.Client
import org.freedesktop.wayland.server.WlDataDeviceRequestsV3
import org.freedesktop.wayland.server.WlDataDeviceResource
import org.freedesktop.wayland.server.WlDataSourceResource
import org.freedesktop.wayland.server.WlSurfaceResource

import javax.annotation.Nonnegative
import javax.inject.Inject
import java.util.Collections
import java.util.WeakHashMap

class WlDataDevice @Inject
internal constructor() : WlDataDeviceRequestsV3, ProtocolObject<WlDataDeviceResource> {

    private val resources = Collections.newSetFromMap(WeakHashMap<WlDataDeviceResource, Boolean>())

    override fun startDrag(requester: WlDataDeviceResource,
                           source: WlDataSourceResource?,
                           origin: WlSurfaceResource,
                           icon: WlSurfaceResource?,
                           serial: Int) {

    }

    override fun setSelection(requester: WlDataDeviceResource,
                              source: WlDataSourceResource?,
                              serial: Int) {

    }

    override fun release(requester: WlDataDeviceResource) {

    }

    override fun create(client: Client,
                        @Nonnegative version: Int,
                        id: Int): WlDataDeviceResource {
        return WlDataDeviceResource(client,
                version,
                id,
                this)
    }

    override fun getResources(): MutableSet<WlDataDeviceResource> {
        return this.resources
    }
}
