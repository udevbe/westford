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
import org.freedesktop.wayland.server.WlTouchRequestsV5
import org.freedesktop.wayland.server.WlTouchResource
import org.westford.compositor.core.TouchDevice
import java.util.*
import javax.annotation.Nonnegative
import javax.inject.Inject

class WlTouch @Inject internal constructor(val touchDevice: TouchDevice) : WlTouchRequestsV5, ProtocolObject<WlTouchResource> {

    override val resources: MutableSet<WlTouchResource> = Collections.newSetFromMap(WeakHashMap<WlTouchResource, Boolean>())

    override fun release(resource: WlTouchResource) = resource.destroy()

    override fun create(client: Client,
                        @Nonnegative version: Int,
                        id: Int): WlTouchResource = WlTouchResource(client,
                                                                    version,
                                                                    id,
                                                                    this)
}
