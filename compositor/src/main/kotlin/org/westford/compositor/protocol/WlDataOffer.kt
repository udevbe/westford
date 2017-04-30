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
import org.freedesktop.wayland.server.WlDataOfferRequestsV3
import org.freedesktop.wayland.server.WlDataOfferResource
import java.util.*
import javax.annotation.Nonnegative

@AutoFactory(className = "WlDataOfferFactory",
             allowSubclasses = true) class WlDataOffer : WlDataOfferRequestsV3, ProtocolObject<WlDataOfferResource> {

    override val resources: MutableSet<WlDataOfferResource> = Collections.newSetFromMap(WeakHashMap<WlDataOfferResource, Boolean>())

    override fun accept(resource: WlDataOfferResource,
                        serial: Int,
                        mimeType: String?) {

    }

    override fun receive(resource: WlDataOfferResource,
                         mimeType: String,
                         fd: Int) {

    }

    override fun destroy(resource: WlDataOfferResource) {
        resource.destroy()
    }

    override fun finish(requester: WlDataOfferResource) {
        //TODO
    }

    override fun setActions(requester: WlDataOfferResource,
                            dndActions: Int,
                            preferredAction: Int) {
        //TODO
    }

    override fun create(client: Client,
                        @Nonnegative version: Int,
                        id: Int): WlDataOfferResource = WlDataOfferResource(client,
                                                                            version,
                                                                            id,
                                                                            this)
}
