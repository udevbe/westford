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
package org.westford.compositor.x11

import org.freedesktop.wayland.shared.WlSeatCapability
import org.westford.compositor.protocol.WlPointerFactory
import org.westford.compositor.protocol.WlSeat
import java.util.*
import javax.inject.Inject

class X11SeatFactory @Inject internal constructor(private val privateX11SeatFactory: PrivateX11SeatFactory,
                                                  private val x11Platform: X11Platform,
                                                  private val x11XkbFactory: X11XkbFactory,
                                                  private val x11InputEventListenerFactory: X11InputEventListenerFactory,
                                                  private val wlSeatFactory: WlSeatFactory,
                                                  private val wlPointerFactory: WlPointerFactory,
                                                  private val wlKeyboardFactory: WlKeyboardFactory,
                                                  private val keyboardDeviceFactory: KeyboardDeviceFactory) {

    fun create(): WlSeat {

        val xcbConnection = this.x11Platform.xcbConnection
        val xkb = this.x11XkbFactory.create(xcbConnection)
        val keyboardDevice = this.keyboardDeviceFactory.create(xkb)
        keyboardDevice.updateKeymap()

        val wlSeat = this.wlSeatFactory.create(this.wlPointerFactory.create(),
                                               this.wlKeyboardFactory.create(keyboardDevice))

        this.x11Platform.x11EventBus.xEventSignal.connect(this.x11InputEventListenerFactory.create(this.privateX11SeatFactory.create(wlSeat)))
        //enable pointer and keyboard for wlseat as an X11 seat always has these.
        wlSeat.getSeat().setCapabilities(EnumSet.of(WlSeatCapability.KEYBOARD,
                                                    WlSeatCapability.POINTER))

        return wlSeat
    }
}
