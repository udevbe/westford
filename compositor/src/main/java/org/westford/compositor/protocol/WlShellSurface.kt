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
import org.freedesktop.wayland.shared.WlShellSurfaceTransient
import org.westford.compositor.wlshell.ShellSurface

import javax.annotation.Nonnegative
import java.util.Collections
import java.util.EnumSet
import java.util.Optional
import java.util.WeakHashMap

@AutoFactory(className = "WlShellSurfaceFactory", allowSubclasses = true)
class WlShellSurface internal constructor(val shellSurface: ShellSurface,
                                          val wlSurfaceResource: WlSurfaceResource) : WlShellSurfaceRequests, ProtocolObject<WlShellSurfaceResource> {

    private val resources = Collections.newSetFromMap(WeakHashMap<WlShellSurfaceResource, Boolean>())

    override fun pong(requester: WlShellSurfaceResource,
                      serial: Int) {
        this.shellSurface.pong(requester,
                serial)
    }

    override fun move(requester: WlShellSurfaceResource,
                      seat: WlSeatResource,
                      serial: Int) {
        val wlSeat = seat.implementation as WlSeat
        wlSeat.getWlPointerResource(seat)
                .ifPresent { wlPointerResource ->
                    shellSurface.move(wlSurfaceResource,
                            wlPointerResource,
                            serial)
                }
    }

    override fun resize(requester: WlShellSurfaceResource,
                        seat: WlSeatResource,
                        serial: Int,
                        edges: Int) {
        val wlSeat = seat.implementation as WlSeat
        wlSeat.getWlPointerResource(seat)
                .ifPresent { wlPointerResource ->
                    shellSurface.resize(requester,
                            wlSurfaceResource,
                            wlPointerResource,
                            serial,
                            edges)
                }
    }

    override fun setToplevel(requester: WlShellSurfaceResource) {
        shellSurface.setTopLevel(wlSurfaceResource)
    }

    override fun setTransient(requester: WlShellSurfaceResource,
                              parent: WlSurfaceResource,
                              x: Int,
                              y: Int,
                              flags: Int) {
        val transientFlags = EnumSet.noneOf<WlShellSurfaceTransient>(WlShellSurfaceTransient::class.java)
        for (wlShellSurfaceTransient in WlShellSurfaceTransient.values()) {
            if (wlShellSurfaceTransient.value and flags != 0) {
                transientFlags.add(wlShellSurfaceTransient)
            }
        }

        shellSurface.setTransient(wlSurfaceResource,
                parent,
                x,
                y,
                transientFlags)
    }

    override fun setFullscreen(requester: WlShellSurfaceResource,
                               method: Int,
                               framerate: Int,
                               output: WlOutputResource?) {

    }

    override fun setPopup(requester: WlShellSurfaceResource,
                          seat: WlSeatResource,
                          serial: Int,
                          parent: WlSurfaceResource,
                          x: Int,
                          y: Int,
                          flags: Int) {

    }

    override fun setMaximized(requester: WlShellSurfaceResource,
                              output: WlOutputResource?) {

    }

    override fun setTitle(requester: WlShellSurfaceResource,
                          title: String) {
        this.shellSurface.title = Optional.of(title)
    }

    override fun setClass(requester: WlShellSurfaceResource,
                          class_: String) {
        this.shellSurface.clazz = Optional.of(class_)
    }

    override fun create(client: Client,
                        @Nonnegative version: Int,
                        id: Int): WlShellSurfaceResource {
        return WlShellSurfaceResource(client,
                version,
                id,
                this)
    }

    override fun getResources(): MutableSet<WlShellSurfaceResource> {
        return this.resources
    }
}
