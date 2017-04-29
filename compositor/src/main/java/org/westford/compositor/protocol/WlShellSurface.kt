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
import org.freedesktop.wayland.server.WlOutputResource
import org.freedesktop.wayland.server.WlSeatResource
import org.freedesktop.wayland.server.WlShellSurfaceRequests
import org.freedesktop.wayland.server.WlShellSurfaceResource
import org.freedesktop.wayland.server.WlSurfaceResource
import org.westford.compositor.wlshell.ShellSurface
import java.util.*
import javax.annotation.Nonnegative

@AutoFactory(className = "WlShellSurfaceFactory",
             allowSubclasses = true) class WlShellSurface internal constructor(val shellSurface: ShellSurface,
                                                                               val wlSurfaceResource: WlSurfaceResource) : WlShellSurfaceRequests, ProtocolObject<WlShellSurfaceResource> {

    override val resources: MutableSet<WlShellSurfaceResource> = Collections.newSetFromMap(WeakHashMap<WlShellSurfaceResource, Boolean>())

    override fun pong(requester: WlShellSurfaceResource,
                      serial: Int) = this.shellSurface.pong(requester,
                                                            serial)

    override fun move(requester: WlShellSurfaceResource,
                      seat: WlSeatResource,
                      serial: Int) {
        val wlSeat = seat.implementation as WlSeat
        wlSeat.getWlPointerResource(seat)?.let {
            this.shellSurface.move(this.wlSurfaceResource,
                                   it,
                                   serial)
        }
    }

    override fun resize(requester: WlShellSurfaceResource,
                        seat: WlSeatResource,
                        serial: Int,
                        edges: Int) {
        val wlSeat = seat.implementation as WlSeat
        wlSeat.getWlPointerResource(seat)?.let {
            this.shellSurface.resize(requester,
                                     this.wlSurfaceResource,
                                     it,
                                     serial,
                                     edges)
        }
    }

    override fun setToplevel(requester: WlShellSurfaceResource) = this.shellSurface.setTopLevel(this.wlSurfaceResource)

    override fun setTransient(requester: WlShellSurfaceResource,
                              parent: WlSurfaceResource,
                              x: Int,
                              y: Int,
                              flags: Int) = this.shellSurface.setTransient(this.wlSurfaceResource,
                                                                           parent,
                                                                           x,
                                                                           y,
                                                                           flags)

    override fun setFullscreen(requester: WlShellSurfaceResource,
                               method: Int,
                               framerate: Int,
                               output: WlOutputResource?) {
        //TODO
    }

    override fun setPopup(requester: WlShellSurfaceResource,
                          seat: WlSeatResource,
                          serial: Int,
                          parent: WlSurfaceResource,
                          x: Int,
                          y: Int,
                          flags: Int) {
        //TODO
    }

    override fun setMaximized(requester: WlShellSurfaceResource,
                              output: WlOutputResource?) {

    }

    override fun setTitle(requester: WlShellSurfaceResource,
                          title: String) {
        this.shellSurface.title = title
    }

    override fun setClass(requester: WlShellSurfaceResource,
                          class_: String) {
        this.shellSurface.clazz = class_
    }

    override fun create(client: Client,
                        @Nonnegative version: Int,
                        id: Int): WlShellSurfaceResource = WlShellSurfaceResource(client,
                                                                                  version,
                                                                                  id,
                                                                                  this)
}
