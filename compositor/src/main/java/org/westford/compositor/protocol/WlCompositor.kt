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
import org.freedesktop.wayland.server.WlCompositorRequestsV4
import org.freedesktop.wayland.server.WlCompositorResource
import org.westford.compositor.core.Compositor
import org.westford.compositor.core.Point
import org.westford.compositor.core.Renderer
import org.westford.compositor.core.Scene
import org.westford.compositor.core.Sibling
import java.util.*
import javax.annotation.Nonnegative
import javax.inject.Inject
import javax.inject.Singleton

@Singleton class WlCompositor @Inject internal constructor(display: Display,
                                                           private val wlSurfaceFactory: WlSurfaceFactory,
                                                           private val wlRegionFactory: WlRegionFactory,
                                                           private val finiteRegionFactory: org.westford.compositor.core.FiniteRegionFactory,
                                                           private val surfaceFactory: org.westford.compositor.core.SurfaceFactory,
                                                           private val compositor: Compositor,
                                                           private val scene: Scene,
                                                           private val renderer: Renderer) : Global<WlCompositorResource>(display,
                                                                                                                          WlCompositorResource::class.java,
                                                                                                                          WlCompositorRequestsV4.VERSION), WlCompositorRequestsV4, ProtocolObject<WlCompositorResource> {

    override val resources: MutableSet<WlCompositorResource> = Collections.newSetFromMap(WeakHashMap<WlCompositorResource, Boolean>())

    override fun onBindClient(client: Client,
                              version: Int,
                              id: Int): WlCompositorResource = add(client,
                                                                   version,
                                                                   id)

    override fun createSurface(compositorResource: WlCompositorResource,
                               id: Int) {
        val surface = this.surfaceFactory.create()
        val wlSurface = this.wlSurfaceFactory.create(surface)

        val wlSurfaceResource = wlSurface.add(compositorResource.client,
                                              compositorResource.version,
                                              id)
        //TODO we might want to move view creation to role object
        surface.createView(wlSurfaceResource,
                           Point.ZERO)
        surface.getSiblings().add(Sibling.create(wlSurfaceResource))

        //TODO unit test destroy handler
        wlSurfaceResource.register {
            this.scene.removeAllViews(wlSurfaceResource)
            surface.markDestroyed()
            this.renderer.onDestroy(wlSurfaceResource)
            this.compositor.requestRender()
        }
    }

    override fun createRegion(resource: WlCompositorResource,
                              id: Int) {
        this.wlRegionFactory.create(this.finiteRegionFactory.create()).add(resource.client,
                                                                           resource.version,
                                                                           id)
    }

    override fun create(client: Client,
                        @Nonnegative version: Int,
                        id: Int): WlCompositorResource = WlCompositorResource(client,
                                                                              version,
                                                                              id,
                                                                              this)
}
