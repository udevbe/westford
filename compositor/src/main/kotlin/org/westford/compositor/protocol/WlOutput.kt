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
import org.freedesktop.wayland.server.*
import org.westford.compositor.core.Output
import java.util.*
import javax.annotation.Nonnegative

@AutoFactory(className = "WlOutputFactory",
             allowSubclasses = true) class WlOutput(@Provided display: Display,
                                                    val output: Output) : Global<WlOutputResource>(display,
                                                                                                   WlOutputResource::class.java,
                                                                                                   WlOutputRequestsV2.VERSION), WlOutputRequestsV2, ProtocolObject<WlOutputResource> {

    override val resources: MutableSet<WlOutputResource> = Collections.newSetFromMap(WeakHashMap<WlOutputResource, Boolean>())

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
                        id: Int): WlOutputResource = WlOutputResource(client,
                                                                      version,
                                                                      id,
                                                                      this)
}
