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
import org.freedesktop.wayland.server.WlDataSourceRequestsV3
import org.freedesktop.wayland.server.WlDataSourceResource

import javax.annotation.Nonnegative
import java.util.ArrayList
import java.util.Collections
import java.util.WeakHashMap

@AutoFactory(className = "WlDataSourceFactory", allowSubclasses = true)
class WlDataSource internal constructor() : WlDataSourceRequestsV3, ProtocolObject<WlDataSourceResource> {

    private val resources = Collections.newSetFromMap(WeakHashMap<WlDataSourceResource, Boolean>())
    private val mimeTypes = ArrayList<String>()

    override fun offer(resource: WlDataSourceResource,
                       mimeType: String) {
        this.mimeTypes.add(mimeType)
    }

    override fun destroy(resource: WlDataSourceResource) {
        resource.destroy()
    }

    override fun setActions(requester: WlDataSourceResource,
                            dndActions: Int) {
        //TODO
    }

    override fun create(client: Client,
                        @Nonnegative version: Int,
                        id: Int): WlDataSourceResource {
        return WlDataSourceResource(client,
                version,
                id,
                this)
    }

    override fun getResources(): MutableSet<WlDataSourceResource> {
        return this.resources
    }
}
