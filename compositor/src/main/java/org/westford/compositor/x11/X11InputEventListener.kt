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
import org.westford.nativ.libxcb.*
import org.westford.nativ.libxcb.Libxcb.Companion.XCB_BUTTON_PRESS
import org.westford.nativ.libxcb.Libxcb.Companion.XCB_BUTTON_RELEASE
import org.westford.nativ.libxcb.Libxcb.Companion.XCB_ENTER_NOTIFY
import org.westford.nativ.libxcb.Libxcb.Companion.XCB_EXPOSE
import org.westford.nativ.libxcb.Libxcb.Companion.XCB_FOCUS_IN
import org.westford.nativ.libxcb.Libxcb.Companion.XCB_FOCUS_OUT
import org.westford.nativ.libxcb.Libxcb.Companion.XCB_KEYMAP_NOTIFY
import org.westford.nativ.libxcb.Libxcb.Companion.XCB_KEY_PRESS
import org.westford.nativ.libxcb.Libxcb.Companion.XCB_KEY_RELEASE
import org.westford.nativ.libxcb.Libxcb.Companion.XCB_LEAVE_NOTIFY
import org.westford.nativ.libxcb.Libxcb.Companion.XCB_MAPPING_NOTIFY
import org.westford.nativ.libxcb.Libxcb.Companion.XCB_MOTION_NOTIFY

@AutoFactory(className = "X11InputEventListenerFactory",
             allowSubclasses = true) class X11InputEventListener(@param:Provided private val x11XkbFactory: X11XkbFactory,
                                                                 @param:Provided private val x11Platform: X11Platform,
                                                                 private val x11Seat: X11Seat) : (Pointer<xcb_generic_event_t>) -> Unit {

    override fun invoke(event: Pointer<xcb_generic_event_t>) {
        val responseType = event.dref().response_type() and 0x7f
        when (responseType) {
            XCB_MOTION_NOTIFY  -> {
                val motion_notify_event = event.castp<xcb_motion_notify_event_t>(xcb_motion_notify_event_t::class.java)
                handle(motion_notify_event.dref())
            }
            XCB_BUTTON_PRESS   -> {
                val button_press_event = event.castp<xcb_button_press_event_t>(xcb_button_press_event_t::class.java)
                handle(button_press_event.dref())
            }
            XCB_BUTTON_RELEASE -> {
                val button_release_event = event.castp<xcb_button_release_event_t>(xcb_button_release_event_t::class.java)
                handle(button_release_event.dref())
            }
            XCB_KEY_PRESS      -> {
                val key_press_event = event.castp<xcb_key_press_event_t>(xcb_key_press_event_t::class.java)
                handle(key_press_event.dref())
            }
            XCB_KEY_RELEASE    -> {
                val key_release_event = event.castp<xcb_key_release_event_t>(xcb_key_release_event_t::class.java)
                handle(key_release_event.dref())
            }
            XCB_EXPOSE         -> {
            }//                final Pointer<xcb_expose_event_t> expose_event = event.castp(xcb_expose_event_t.class);
        //handle(expose_event);
            XCB_ENTER_NOTIFY   -> {
            }//               final Pointer<xcb_enter_notify_event_t> enter_notify_event = event.castp(xcb_enter_notify_event_t.class);
        //handle(enter_notify_event);
            XCB_LEAVE_NOTIFY   -> {
            }//                final Pointer<xcb_leave_notify_event_t> leave_notify_event = event.castp(xcb_leave_notify_event_t.class);
        //handle(leave_notify_event);
            XCB_FOCUS_IN       -> {
            }//                final Pointer<xcb_focus_in_event_t> focus_in_event = event.castp(xcb_focus_in_event_t.class);
        //handle(focus_in_event);
            XCB_FOCUS_OUT      -> {
            }//                final Pointer<xcb_focus_out_event_t> focus_out_event = event.castp(xcb_focus_out_event_t.class);
        //handle(focus_out_event);
            XCB_KEYMAP_NOTIFY  -> {
            }//                final Pointer<xcb_keymap_notify_event_t> keymap_notify_event = event.castp(xcb_keymap_notify_event_t.class);
        //                handle(keymap_notify_event.dref());
            XCB_MAPPING_NOTIFY -> {
                val mapping_notify_event = event.castp<xcb_mapping_notify_event_t>(xcb_mapping_notify_event_t::class.java)
                handle(mapping_notify_event.dref())
            }
        }
    }

    private fun handle(event: xcb_motion_notify_event_t) {
        this.x11Seat.deliverMotion(event.event(),
                                   event.time(),
                                   event.event_x(),
                                   event.event_y())
    }

    private fun handle(event: xcb_button_press_event_t) {
        this.x11Seat.deliverButton(event.event(),
                                   event.time(),
                                   event.detail(),
                                   true)
    }

    private fun handle(event: xcb_button_release_event_t) {
        this.x11Seat.deliverButton(event.event(),
                                   event.time(),
                                   event.detail(),
                                   false)
    }

    private fun handle(event: xcb_key_press_event_t) {
        this.x11Seat.deliverKey(event.time(),
                                event.detail(),
                                true)
    }

    private fun handle(event: xcb_key_release_event_t) {
        this.x11Seat.deliverKey(event.time(),
                                event.detail(),
                                false)
    }

    private fun handle(event: xcb_mapping_notify_event_t) {
        if (event.request() === XCB_MAPPING_KEYBOARD) {
            val wlKeyboard = this.x11Seat.wlSeat.wlKeyboard
            val keyboardDevice = wlKeyboard.keyboardDevice
            val xkb = this.x11XkbFactory.create(this.x11Platform.xcbConnection)
            keyboardDevice.xkb = xkb
            keyboardDevice.updateKeymap()
            keyboardDevice.emitKeymap(wlKeyboard.resources)
        }
    }
}
