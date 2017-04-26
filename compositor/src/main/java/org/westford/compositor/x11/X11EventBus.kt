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

import com.google.auto.factory.AutoFactory
import com.google.auto.factory.Provided
import org.freedesktop.jaccall.Pointer
import org.freedesktop.wayland.server.EventLoop
import org.westford.Signal
import org.westford.Slot
import org.westford.nativ.libxcb.Libxcb
import org.westford.nativ.libxcb.xcb_generic_event_t

@AutoFactory(className = "X11EventBusFactory", allowSubclasses = true)
class X11EventBus internal constructor(@param:Provided private val libxcb: Libxcb,
                                       private val xcbConnection: Long) : EventLoop.FileDescriptorEventHandler {

    val xEventSignal = Signal<Pointer<xcb_generic_event_t>, Slot<Pointer<xcb_generic_event_t>>>()

    override fun handle(fd: Int,
                        mask: Int): Int {
        var event: Long
        while ((event = this.libxcb.xcb_poll_for_event(this.xcbConnection)) != 0L) {
            Pointer.wrap<xcb_generic_event_t>(xcb_generic_event_t::class.java!!,
                    event).use { generic_event -> xEventSignal.emit(generic_event) }
        }
        this.libxcb.xcb_flush(this.xcbConnection)
        return 0
    }
}
