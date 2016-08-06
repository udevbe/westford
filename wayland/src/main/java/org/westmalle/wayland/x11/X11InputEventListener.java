/*
 * Westmalle Wayland Compositor.
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
package org.westmalle.wayland.x11;


import org.freedesktop.jaccall.Pointer;
import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import org.westmalle.wayland.core.KeyboardDevice;
import org.westmalle.wayland.core.Xkb;
import org.westmalle.wayland.core.events.Slot;
import org.westmalle.wayland.nativ.libxcb.xcb_button_press_event_t;
import org.westmalle.wayland.nativ.libxcb.xcb_button_release_event_t;
import org.westmalle.wayland.nativ.libxcb.xcb_generic_event_t;
import org.westmalle.wayland.nativ.libxcb.xcb_key_press_event_t;
import org.westmalle.wayland.nativ.libxcb.xcb_key_release_event_t;
import org.westmalle.wayland.nativ.libxcb.xcb_mapping_notify_event_t;
import org.westmalle.wayland.nativ.libxcb.xcb_motion_notify_event_t;
import org.westmalle.wayland.protocol.WlKeyboard;

import javax.annotation.Nonnull;

import static org.westmalle.wayland.nativ.libxcb.Libxcb.XCB_BUTTON_PRESS;
import static org.westmalle.wayland.nativ.libxcb.Libxcb.XCB_BUTTON_RELEASE;
import static org.westmalle.wayland.nativ.libxcb.Libxcb.XCB_ENTER_NOTIFY;
import static org.westmalle.wayland.nativ.libxcb.Libxcb.XCB_EXPOSE;
import static org.westmalle.wayland.nativ.libxcb.Libxcb.XCB_FOCUS_IN;
import static org.westmalle.wayland.nativ.libxcb.Libxcb.XCB_FOCUS_OUT;
import static org.westmalle.wayland.nativ.libxcb.Libxcb.XCB_KEYMAP_NOTIFY;
import static org.westmalle.wayland.nativ.libxcb.Libxcb.XCB_KEY_PRESS;
import static org.westmalle.wayland.nativ.libxcb.Libxcb.XCB_KEY_RELEASE;
import static org.westmalle.wayland.nativ.libxcb.Libxcb.XCB_LEAVE_NOTIFY;
import static org.westmalle.wayland.nativ.libxcb.Libxcb.XCB_MAPPING_KEYBOARD;
import static org.westmalle.wayland.nativ.libxcb.Libxcb.XCB_MAPPING_NOTIFY;
import static org.westmalle.wayland.nativ.libxcb.Libxcb.XCB_MOTION_NOTIFY;

@AutoFactory(className = "X11InputEventListenerFactory",
             allowSubclasses = true)
public class X11InputEventListener implements Slot<Pointer<xcb_generic_event_t>> {

    @Nonnull
    private final X11XkbFactory     x11XkbFactory;
    @Nonnull
    private final X11RenderPlatform x11Platform;
    @Nonnull
    private final X11Seat           x11Seat;

    X11InputEventListener(@Provided @Nonnull final X11XkbFactory x11XkbFactory,
                          @Provided @Nonnull final X11RenderPlatform x11Platform,
                          @Nonnull final X11Seat x11Seat) {
        this.x11XkbFactory = x11XkbFactory;
        this.x11Platform = x11Platform;
        this.x11Seat = x11Seat;
    }

    public void handle(@Nonnull final Pointer<xcb_generic_event_t> event) {
        final int responseType = (event.dref()
                                       .response_type() & 0x7f);
        switch (responseType) {
            case XCB_MOTION_NOTIFY: {
                final Pointer<xcb_motion_notify_event_t> motion_notify_event = event.castp(xcb_motion_notify_event_t.class);
                handle(motion_notify_event.dref());
                break;
            }
            case XCB_BUTTON_PRESS: {
                final Pointer<xcb_button_press_event_t> button_press_event = event.castp(xcb_button_press_event_t.class);
                handle(button_press_event.dref());
                break;
            }
            case XCB_BUTTON_RELEASE: {
                final Pointer<xcb_button_release_event_t> button_release_event = event.castp(xcb_button_release_event_t.class);
                handle(button_release_event.dref());
                break;
            }
            case XCB_KEY_PRESS: {
                final Pointer<xcb_key_press_event_t> key_press_event = event.castp(xcb_key_press_event_t.class);
                handle(key_press_event.dref());
                break;
            }
            case XCB_KEY_RELEASE: {
                final Pointer<xcb_key_release_event_t> key_release_event = event.castp(xcb_key_release_event_t.class);
                handle(key_release_event.dref());
                break;
            }
            case XCB_EXPOSE: {
//                final Pointer<xcb_expose_event_t> expose_event = event.castp(xcb_expose_event_t.class);
                //handle(expose_event);
                break;
            }
            case XCB_ENTER_NOTIFY: {
                //               final Pointer<xcb_enter_notify_event_t> enter_notify_event = event.castp(xcb_enter_notify_event_t.class);
                //handle(enter_notify_event);
                break;
            }
            case XCB_LEAVE_NOTIFY: {
//                final Pointer<xcb_leave_notify_event_t> leave_notify_event = event.castp(xcb_leave_notify_event_t.class);
                //handle(leave_notify_event);
                break;
            }
            case XCB_FOCUS_IN: {
//                final Pointer<xcb_focus_in_event_t> focus_in_event = event.castp(xcb_focus_in_event_t.class);
                //handle(focus_in_event);
                break;
            }
            case XCB_FOCUS_OUT: {
//                final Pointer<xcb_focus_out_event_t> focus_out_event = event.castp(xcb_focus_out_event_t.class);
                //handle(focus_out_event);
                break;
            }
            case XCB_KEYMAP_NOTIFY: {
//                final Pointer<xcb_keymap_notify_event_t> keymap_notify_event = event.castp(xcb_keymap_notify_event_t.class);
//                handle(keymap_notify_event.dref());
                break;
            }
            case XCB_MAPPING_NOTIFY: {
                final Pointer<xcb_mapping_notify_event_t> mapping_notify_event = event.castp(xcb_mapping_notify_event_t.class);
                handle(mapping_notify_event.dref());
                break;
            }
        }
    }

    private void handle(final xcb_motion_notify_event_t event) {
        this.x11Seat.deliverMotion(event.event(),
                                   event.time(),
                                   event.event_x(),
                                   event.event_y());
    }

    private void handle(final xcb_button_press_event_t event) {
        this.x11Seat.deliverButton(event.event(),
                                   event.time(),
                                   event.detail(),
                                   true);
    }

    private void handle(final xcb_button_release_event_t event) {
        this.x11Seat.deliverButton(event.event(),
                                   event.time(),
                                   event.detail(),
                                   false);
    }

    private void handle(final xcb_key_press_event_t event) {
        this.x11Seat.deliverKey(event.time(),
                                event.detail(),
                                true);
    }

    private void handle(final xcb_key_release_event_t event) {
        this.x11Seat.deliverKey(event.time(),
                                event.detail(),
                                false);
    }

    private void handle(final xcb_mapping_notify_event_t event) {
        if (event.request() == XCB_MAPPING_KEYBOARD) {
            final WlKeyboard wlKeyboard = this.x11Seat.getWlSeat()
                                                      .getWlKeyboard();
            final KeyboardDevice keyboardDevice = wlKeyboard.getKeyboardDevice();
            final Xkb            xkb            = this.x11XkbFactory.create(this.x11Platform.getXcbConnection());
            keyboardDevice.setXkb(xkb);
            keyboardDevice.updateKeymap();
            keyboardDevice.emitKeymap(wlKeyboard.getResources());
        }
    }
}
