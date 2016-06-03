//Copyright 2015 Erik De Rijcke
//
//Licensed under the Apache License,Version2.0(the"License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
//http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing,software
//distributed under the License is distributed on an"AS IS"BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
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
import org.westmalle.wayland.protocol.WlSeat;

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
    private final X11XkbFactory x11XkbFactory;
    @Nonnull
    private final X11Seat       x11Seat;

    X11InputEventListener(@Provided @Nonnull final X11XkbFactory x11XkbFactory,
                          @Nonnull final X11Seat x11Seat) {
        this.x11XkbFactory = x11XkbFactory;
        this.x11Seat = x11Seat;
    }

    public void handle(@Nonnull final Pointer<xcb_generic_event_t> event) {
        final int responseType = (event.dref()
                                       .response_type() & ~0x80);
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
        this.x11Seat.deliverMotion(event.time(),
                                   event.event_x(),
                                   event.event_y());
    }

    private void handle(final xcb_button_press_event_t event) {
        this.x11Seat.deliverButton(event.time(),
                                   event.detail(),
                                   true);
    }

    private void handle(final xcb_button_release_event_t event) {
        this.x11Seat.deliverButton(event.time(),
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
            final Xkb xkb = this.x11XkbFactory.create(this.x11Seat.getX11Output()
                                                                  .getXcbConnection());
            keyboardDevice.setXkb(xkb);
            keyboardDevice.updateKeymap();
            keyboardDevice.emitKeymap(wlKeyboard.getResources());
        }
    }
}
