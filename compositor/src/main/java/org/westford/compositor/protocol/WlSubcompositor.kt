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
import org.freedesktop.wayland.server.WlSubcompositorRequests
import org.freedesktop.wayland.server.WlSubcompositorResource
import org.freedesktop.wayland.server.WlSubsurfaceResource
import org.freedesktop.wayland.server.WlSurfaceResource
import org.freedesktop.wayland.shared.WlSubcompositorError
import org.westford.compositor.core.Role
import org.westford.compositor.core.Subsurface
import org.westford.compositor.core.SubsurfaceFactory
import org.westford.compositor.core.Surface

import javax.annotation.Nonnegative
import javax.inject.Inject
import javax.inject.Singleton
import java.util.Collections
import java.util.Optional
import java.util.WeakHashMap

@Singleton
class WlSubcompositor @Inject
internal constructor(display: Display,
                     private val wlSubSurfaceFactory: WlSubsurfaceFactory,
                     private val subsurfaceFactory: SubsurfaceFactory) : Global<WlSubcompositorResource>(display, WlSubcompositorResource::class.java, WlSubcompositorRequests.VERSION), WlSubcompositorRequests, ProtocolObject<WlSubcompositorResource> {

    private val resources = Collections.newSetFromMap(WeakHashMap<WlSubcompositorResource, Boolean>())

    override fun destroy(resource: WlSubcompositorResource) {
        resource.destroy()
    }

    override fun getSubsurface(requester: WlSubcompositorResource,
                               id: Int,
                               wlSurfaceResource: WlSurfaceResource,
                               parentWlSurfaceResource: WlSurfaceResource) {

        val wlSurface = wlSurfaceResource.implementation as WlSurface
        val surface = wlSurface.surface

        val role = surface.role
        val hasRole = role.isPresent

        /*
         * Check if the surface does not have a role or has an inactive subsurface role, both are ok. Otherwise we raise
         * a protocol error.
         */
        if (!hasRole || role.get() is Subsurface && (role.get() as Subsurface).isInert) {

            val subsurface = this.subsurfaceFactory.create(parentWlSurfaceResource,
                    wlSurfaceResource)
            surface.setRole(subsurface)

            if (!hasRole) {

            }

            val wlSubsurface = this.wlSubSurfaceFactory.create(subsurface)
            val wlSubsurfaceResource = wlSubsurface.add(requester.client,
                    requester.version,
                    id)
            wlSurfaceResource.register(DestroyListener { wlSubsurfaceResource.destroy() })
        } else {
            requester.client
                    .getObject(Display.OBJECT_ID)
                    .postError(WlSubcompositorError.BAD_SURFACE.value,
                            String.format("Desired sub surface already has another role (%s)",
                                    role.get().javaClass
                                            .getSimpleName()))
        }
    }

    override fun create(client: Client,
                        @Nonnegative version: Int,
                        id: Int): WlSubcompositorResource {
        return WlSubcompositorResource(client,
                version,
                id,
                this)
    }

    override fun getResources(): MutableSet<WlSubcompositorResource> {
        return this.resources
    }

    override fun onBindClient(client: Client,
                              version: Int,
                              id: Int): WlSubcompositorResource {
        return WlSubcompositorResource(client,
                version,
                id,
                this)
    }
}
