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
import com.google.auto.factory.Provided
import org.freedesktop.wayland.server.Client
import org.freedesktop.wayland.server.Display
import org.freedesktop.wayland.server.Global
import org.freedesktop.wayland.server.WlOutputRequestsV2
import org.freedesktop.wayland.server.WlOutputResource
import org.westford.compositor.core.Output

import javax.annotation.Nonnegative
import java.util.Collections
import java.util.WeakHashMap

@AutoFactory(className = "WlOutputFactory", allowSubclasses = true)
class WlOutput internal constructor(@Provided display: Display,
                                    val output: Output) : Global<WlOutputResource>(display, WlOutputResource::class.java, WlOutputRequestsV2.VERSION), WlOutputRequestsV2, ProtocolObject<WlOutputResource> {

    private val resources = Collections.newSetFromMap(WeakHashMap<WlOutputResource, Boolean>())

    override fun onBindClient(client: Client,
                              version: Int,
                              id: Int): WlOutputResource {
        val wlOutputResource = add(client,
                version,
                id)
        this.output.notifyGeometry(wlOutputResource)
        this.output.notifyMode(wlOutputResource)
        if (wlOutputResource.version >= 2) {
            wlOutputResource.done()
        }
        return wlOutputResource
    }

    override fun create(client: Client,
                        @Nonnegative version: Int,
                        id: Int): WlOutputResource {
        return WlOutputResource(client,
                version,
                id,
                this)
    }

    override fun getResources(): MutableSet<WlOutputResource> {
        return this.resources
    }
}
