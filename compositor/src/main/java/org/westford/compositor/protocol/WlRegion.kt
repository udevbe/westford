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
import org.freedesktop.wayland.server.WlRegionRequests
import org.freedesktop.wayland.server.WlRegionResource
import org.westford.compositor.core.Rectangle
import org.westford.compositor.core.Region

import javax.annotation.Nonnegative
import java.util.Collections
import java.util.WeakHashMap

@AutoFactory(className = "WlRegionFactory", allowSubclasses = true)
class WlRegion internal constructor(val region: Region) : WlRegionRequests, ProtocolObject<WlRegionResource> {

    private val resources = Collections.newSetFromMap(WeakHashMap<WlRegionResource, Boolean>())

    override fun create(client: Client,
                        @Nonnegative version: Int,
                        id: Int): WlRegionResource {
        return WlRegionResource(client,
                version,
                id,
                this)
    }

    override fun getResources(): MutableSet<WlRegionResource> {
        return this.resources
    }

    override fun destroy(resource: WlRegionResource) {
        resource.destroy()
    }

    override fun add(resource: WlRegionResource,
                     x: Int,
                     y: Int,
                     @Nonnegative width: Int,
                     @Nonnegative height: Int) {
        if (width < 0 || height < 0) {
            throw IllegalArgumentException("Got negative width or height")
        }

        this.region.add(Rectangle.create(x,
                y,
                width,
                height))
    }

    override fun subtract(resource: WlRegionResource,
                          x: Int,
                          y: Int,
                          @Nonnegative width: Int,
                          @Nonnegative height: Int) {
        if (width < 0 || height < 0) {
            throw IllegalArgumentException("Got negative width or height")
        }

        this.region.subtract(Rectangle.create(x,
                y,
                width,
                height))
    }
}
