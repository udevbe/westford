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
import org.freedesktop.wayland.server.Display
import org.freedesktop.wayland.server.Global
import org.freedesktop.wayland.server.WlDataDeviceManagerRequestsV3
import org.freedesktop.wayland.server.WlDataDeviceManagerResource
import org.freedesktop.wayland.server.WlSeatResource
import java.util.*
import javax.annotation.Nonnegative
import javax.inject.Inject
import javax.inject.Singleton

@Singleton class WlDataDeviceManager @Inject internal constructor(display: Display,
                                                                  private val wlDataSourceFactory: WlDataSourceFactory) : Global<WlDataDeviceManagerResource>(display,
                                                                                                                                                              WlDataDeviceManagerResource::class.java,
                                                                                                                                                              WlDataDeviceManagerRequestsV3.VERSION), WlDataDeviceManagerRequestsV3, ProtocolObject<WlDataDeviceManagerResource> {

    override val resources: MutableSet<WlDataDeviceManagerResource> = Collections.newSetFromMap(WeakHashMap<WlDataDeviceManagerResource, Boolean>())

    override fun onBindClient(client: Client,
                              version: Int,
                              id: Int): WlDataDeviceManagerResource = add(client,
                                                                          version,
                                                                          id)

    override fun createDataSource(resource: WlDataDeviceManagerResource,
                                  id: Int) {
        this.wlDataSourceFactory.create().add(resource.client,
                                              resource.version,
                                              id)
    }

    override fun getDataDevice(requester: WlDataDeviceManagerResource,
                               id: Int,
                               seat: WlSeatResource) {
        val wlSeat = seat.implementation as WlSeat
        wlSeat.wlDataDevice.add(requester.client,
                                requester.version,
                                id)
    }

    override fun create(client: Client,
                        @Nonnegative version: Int,
                        id: Int): WlDataDeviceManagerResource = WlDataDeviceManagerResource(client,
                                                                                            version,
                                                                                            id,
                                                                                            this)
}
