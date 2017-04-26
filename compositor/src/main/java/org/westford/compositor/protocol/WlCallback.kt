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
import org.freedesktop.wayland.server.WlCallbackRequests
import org.freedesktop.wayland.server.WlCallbackResource

import javax.annotation.Nonnegative
import javax.inject.Inject
import java.util.Collections
import java.util.WeakHashMap

@AutoFactory(className = "WlCallbackFactory", allowSubclasses = true)
class WlCallback @Inject
internal constructor() : WlCallbackRequests, ProtocolObject<WlCallbackResource> {

    private val resources = Collections.newSetFromMap(WeakHashMap<WlCallbackResource, Boolean>())

    override fun create(client: Client,
                        @Nonnegative version: Int,
                        id: Int): WlCallbackResource {
        return WlCallbackResource(client,
                version,
                id,
                this)
    }

    override fun getResources(): MutableSet<WlCallbackResource> {
        return this.resources
    }
}
