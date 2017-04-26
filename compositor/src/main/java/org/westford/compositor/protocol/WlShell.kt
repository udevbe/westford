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
import org.freedesktop.wayland.server.WlShellRequests
import org.freedesktop.wayland.server.WlShellResource
import org.freedesktop.wayland.server.WlShellSurfaceResource
import org.freedesktop.wayland.server.WlSurfaceResource
import org.freedesktop.wayland.shared.WlShellError
import org.westford.compositor.core.Role
import org.westford.compositor.core.Surface
import org.westford.compositor.wlshell.ShellSurface

import javax.annotation.Nonnegative
import javax.inject.Inject
import javax.inject.Singleton
import java.util.Collections
import java.util.Optional
import java.util.WeakHashMap

@Singleton
class WlShell @Inject
internal constructor(private val display: Display,
                     private val wlShellSurfaceFactory: WlShellSurfaceFactory,
                     private val shellSurfaceFactory: org.westford.compositor.wlshell.ShellSurfaceFactory) : Global<WlShellResource>(display, WlShellResource::class.java, WlShellRequests.VERSION), WlShellRequests, ProtocolObject<WlShellResource> {

    private val resources = Collections.newSetFromMap(WeakHashMap<WlShellResource, Boolean>())

    override fun getShellSurface(requester: WlShellResource,
                                 id: Int,
                                 wlSurfaceResource: WlSurfaceResource) {

        val wlSurface = wlSurfaceResource.implementation as WlSurface
        val surface = wlSurface.surface

        val pingSerial = this.display.nextSerial()

        val role = surface.role
        if (role.isPresent) {
            requester.client
                    .getObject(Display.OBJECT_ID)
                    .postError(WlShellError.ROLE.value,
                            String.format("Desired shell surface already has another role (%s)",
                                    surface.role.javaClass
                                            .getSimpleName()))
            return
        }

        val shellSurface = this.shellSurfaceFactory.create(wlSurfaceResource,
                surface,
                pingSerial)
        surface.setRole(shellSurface)
        val wlShellSurface = this.wlShellSurfaceFactory.create(shellSurface,
                wlSurfaceResource)
        val wlShellSurfaceResource = wlShellSurface.add(requester.client,
                requester.version,
                id)
        wlSurfaceResource.register(DestroyListener { wlShellSurfaceResource.destroy() })

        shellSurface.pong(wlShellSurfaceResource,
                pingSerial)
    }

    override fun onBindClient(client: Client,
                              version: Int,
                              id: Int): WlShellResource {
        //FIXME check if we support requested version.
        return add(client,
                version,
                id)
    }

    override fun create(client: Client,
                        @Nonnegative version: Int,
                        id: Int): WlShellResource {
        return WlShellResource(client,
                version,
                id,
                this)
    }

    override fun getResources(): MutableSet<WlShellResource> {
        return this.resources
    }
}
