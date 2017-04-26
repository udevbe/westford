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
import org.freedesktop.wayland.server.Resource

import javax.annotation.Nonnegative

interface ProtocolObject<T : Resource<*>> {

    /**
     * Associate a new resource object with this protocol object.

     * @param client  The client owning the newly created resource.
     * *
     * @param version The version desired by the client for the new resource.
     * *
     * @param id      The id for the new resource, as provided by the client
     * *
     * *
     * @return the newly created resource.
     */
    fun add(client: Client,
            version: Int,
            id: Int): T {
        //FIXME check if version is supported by compositor.

        val resource = create(client,
                version,
                id)
        resource.register { resources.remove(resource) }
        resources.add(resource)
        return resource
    }

    /**
     * Create a resource.

     * @param client  The client owning the newly created resource.
     * *
     * @param version The version desired by the client for the new resource.
     * *
     * @param id      The id for the new resource, as provided by the client
     * *
     * *
     * @return the newly created resource.
     */
    fun create(client: Client,
               @Nonnegative version: Int,
               id: Int): T

    /**
     * Get all resources currently associated with this protocol object.

     * @return All associated resources.
     */
    val resources: MutableSet<T>
}
