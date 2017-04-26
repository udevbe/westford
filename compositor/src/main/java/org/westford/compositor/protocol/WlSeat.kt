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
import org.freedesktop.wayland.server.Display
import org.freedesktop.wayland.server.Global
import org.freedesktop.wayland.server.WlKeyboardResource
import org.freedesktop.wayland.server.WlPointerResource
import org.freedesktop.wayland.server.WlSeatRequestsV5
import org.freedesktop.wayland.server.WlSeatResource
import org.freedesktop.wayland.server.WlTouchResource
import org.westford.compositor.core.Seat

import javax.annotation.Nonnegative
import java.util.Collections
import java.util.HashMap
import java.util.Optional
import java.util.WeakHashMap

@AutoFactory(className = "WlSeatFactory", allowSubclasses = true)
class WlSeat internal constructor(@Provided display: Display,
                                  @param:Provided val wlDataDevice: WlDataDevice,
                                  @param:Provided val seat: Seat,
                                  val wlPointer: WlPointer,
                                  val wlKeyboard: WlKeyboard,
                                  @param:Provided val wlTouch: WlTouch) : Global<WlSeatResource>(display, WlSeatResource::class.java, WlSeatRequestsV5.VERSION), WlSeatRequestsV5, ProtocolObject<WlSeatResource> {

    private val resources = Collections.newSetFromMap(WeakHashMap<WlSeatResource, Boolean>())

    private val wlPointerResources = HashMap<WlSeatResource, WlPointerResource>()
    private val wlKeyboardResources = HashMap<WlSeatResource, WlKeyboardResource>()
    private val wlTouchResources = HashMap<WlSeatResource, WlTouchResource>()

    override fun onBindClient(client: Client,
                              version: Int,
                              id: Int): WlSeatResource {
        //FIXME check if we support given version.
        val wlSeatResource = add(client,
                version,
                id)
        wlSeatResource.register {
            this@WlSeat.wlPointerResources.remove(wlSeatResource)
            this@WlSeat.wlKeyboardResources.remove(wlSeatResource)
            this@WlSeat.wlTouchResources.remove(wlSeatResource)
        }

        seat.emitCapabilities(setOf<WlSeatResource>(wlSeatResource))

        return wlSeatResource
    }

    override fun getPointer(wlSeatResource: WlSeatResource,
                            id: Int) {
        val wlPointerResource = wlPointer.add(wlSeatResource.client,
                wlSeatResource.version,
                id)
        this.wlPointerResources.put(wlSeatResource,
                wlPointerResource)
        wlPointerResource.register { this@WlSeat.wlPointerResources.remove(wlSeatResource) }
    }

    override fun getKeyboard(wlSeatResource: WlSeatResource,
                             id: Int) {
        val wlKeyboard = wlKeyboard
        val wlKeyboardResource = wlKeyboard.add(wlSeatResource.client,
                wlSeatResource.version,
                id)
        this.wlKeyboardResources.put(wlSeatResource,
                wlKeyboardResource)
        wlKeyboardResource.register { this@WlSeat.wlKeyboardResources.remove(wlSeatResource) }

        wlKeyboard.keyboardDevice
                .emitKeymap(setOf<WlKeyboardResource>(wlKeyboardResource))
    }

    override fun getTouch(wlSeatResource: WlSeatResource,
                          id: Int) {
        val wlTouchResource = wlTouch.add(wlSeatResource.client,
                wlSeatResource.version,
                id)
        this.wlTouchResources.put(wlSeatResource,
                wlTouchResource)
        wlTouchResource.register { this@WlSeat.wlTouchResources.remove(wlSeatResource) }
    }

    override fun release(requester: WlSeatResource) {
        //TODO
    }

    fun getWlKeyboardResource(wlSeatResource: WlSeatResource): Optional<WlKeyboardResource> {
        return Optional.ofNullable(this.wlKeyboardResources[wlSeatResource])
    }

    override fun getResources(): MutableSet<WlSeatResource> {
        return this.resources
    }

    override fun create(client: Client,
                        @Nonnegative version: Int,
                        id: Int): WlSeatResource {
        return WlSeatResource(client,
                version,
                id,
                this)
    }

    fun getWlPointerResource(wlSeatResource: WlSeatResource): Optional<WlPointerResource> {
        return Optional.ofNullable(this.wlPointerResources[wlSeatResource])
    }

    fun getWlTouchResource(wlSeatResource: WlSeatResource): Optional<WlTouchResource> {
        return Optional.ofNullable(this.wlTouchResources[wlSeatResource])
    }
}