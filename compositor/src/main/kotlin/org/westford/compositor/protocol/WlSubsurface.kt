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
import org.freedesktop.wayland.server.WlSubsurfaceRequests
import org.freedesktop.wayland.server.WlSubsurfaceResource
import org.freedesktop.wayland.server.WlSurfaceResource
import org.freedesktop.wayland.shared.WlSubsurfaceError
import org.westford.compositor.core.Point
import org.westford.compositor.core.Scene
import org.westford.compositor.core.Sibling
import org.westford.compositor.core.Subsurface
import java.util.*
import javax.annotation.Nonnegative

@AutoFactory(className = "WlSubsurfaceFactory") class WlSubsurface(@param:Provided private val scene: Scene,
                                                                   val subsurface: Subsurface) : WlSubsurfaceRequests, ProtocolObject<WlSubsurfaceResource> {

    override val resources: MutableSet<WlSubsurfaceResource> = Collections.newSetFromMap(WeakHashMap<WlSubsurfaceResource, Boolean>())

    override fun create(client: Client,
                        @Nonnegative version: Int,
                        id: Int): WlSubsurfaceResource = WlSubsurfaceResource(client,
                                                                              version,
                                                                              id,
                                                                              this)

    override fun destroy(resource: WlSubsurfaceResource) = resource.destroy()

    override fun setPosition(wlSubsurfaceResource: WlSubsurfaceResource,
                             x: Int,
                             y: Int) = subsurface.setPosition(Point(x,
                                                                    y))

    override fun placeAbove(requester: WlSubsurfaceResource,
                            sibling: WlSurfaceResource) {
        //TODO unit test
        if (isValid(requester,
                    sibling)) {
            subsurface.above(sibling)
        }
        else {
            requester.postError(WlSubsurfaceError.BAD_SURFACE.value,
                                "placeAbove request failed. wl_surface is not a sibling or the parent")
        }
    }

    private fun isValid(requester: WlSubsurfaceResource,
                        siblingWlSurfaceResource: WlSurfaceResource): Boolean {
        val subsurface = subsurface
        if (subsurface.isInert) {
            /*
             * we return true here as a the docs say that a subsurface with a destroyed parent should become inert
             * ie we don't care what the sibling argument is, as the request will be ignored anyway.
             */
            return true
        }

        val wlSubsurface = requester.implementation as WlSubsurface
        val parentWlSurface = wlSubsurface.subsurface.parentWlSurfaceResource.implementation as WlSurface
        return parentWlSurface.surface.siblings.contains(Sibling(siblingWlSurfaceResource))
    }

    override fun placeBelow(requester: WlSubsurfaceResource,
                            sibling: WlSurfaceResource) {
        //TODO unit test
        if (isValid(requester,
                    sibling)) {
            subsurface.below(sibling)
        }
        else {
            requester.postError(WlSubsurfaceError.BAD_SURFACE.value,
                                "placeBelow request failed. wl_surface is not a sibling or the parent")
        }
    }

    override fun setSync(requester: WlSubsurfaceResource) = subsurface.setSync(true)

    override fun setDesync(requester: WlSubsurfaceResource) = subsurface.setSync(false)
}
