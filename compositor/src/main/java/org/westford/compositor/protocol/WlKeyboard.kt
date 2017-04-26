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
import org.freedesktop.wayland.server.WlKeyboardRequestsV5
import org.freedesktop.wayland.server.WlKeyboardResource
import org.westford.compositor.core.KeyboardDevice

import javax.annotation.Nonnegative
import java.util.Collections
import java.util.WeakHashMap

@AutoFactory(className = "WlKeyboardFactory", allowSubclasses = true)
class WlKeyboard internal constructor(val keyboardDevice: KeyboardDevice) : WlKeyboardRequestsV5, ProtocolObject<WlKeyboardResource> {

    private val resources = Collections.newSetFromMap(WeakHashMap<WlKeyboardResource, Boolean>())

    override fun release(resource: WlKeyboardResource) {
        resource.destroy()
    }

    override fun create(client: Client,
                        @Nonnegative version: Int,
                        id: Int): WlKeyboardResource {
        return WlKeyboardResource(client,
                version,
                id,
                this)
    }

    override fun getResources(): MutableSet<WlKeyboardResource> {
        return this.resources
    }
}
