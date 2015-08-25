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


import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import com.squareup.otto.Subscribe;
import org.westmalle.wayland.core.KeyboardDevice;
import org.westmalle.wayland.core.Xkb;
import org.westmalle.wayland.nativ.libxcb.xcb_button_press_event_t;
import org.westmalle.wayland.nativ.libxcb.xcb_button_release_event_t;
import org.westmalle.wayland.nativ.libxcb.xcb_key_press_event_t;
import org.westmalle.wayland.nativ.libxcb.xcb_key_release_event_t;
import org.westmalle.wayland.nativ.libxcb.xcb_keymap_notify_event_t;
import org.westmalle.wayland.nativ.libxcb.xcb_motion_notify_event_t;
import org.westmalle.wayland.protocol.WlKeyboard;
import org.westmalle.wayland.protocol.WlSeat;

import javax.annotation.Nonnull;

@AutoFactory(className = "X11InputEventListenerFactory")
public class X11InputEventListener {

    @Nonnull
    private final X11XkbFactory x11XkbFactory;
    @Nonnull
    private final WlSeat        wlSeat;

    X11InputEventListener(@Provided @Nonnull final X11XkbFactory x11XkbFactory,
                          @Nonnull final WlSeat wlSeat) {
        this.x11XkbFactory = x11XkbFactory;
        this.wlSeat = wlSeat;
    }

    @Subscribe
    public void handle(final xcb_key_press_event_t event) {
        final X11Seat x11Seat = (X11Seat) this.wlSeat.getSeat()
                                                     .getPlatformImplementation();
        x11Seat.deliverKey(this.wlSeat,
                           event.detail,
                           true);
    }

    @Subscribe
    public void handle(final xcb_button_press_event_t event) {
        final X11Seat x11Seat = (X11Seat) this.wlSeat.getSeat()
                                                     .getPlatformImplementation();
        x11Seat.deliverButton(this.wlSeat,
                              event.time,
                              event.detail,
                              true);
    }

    @Subscribe
    public void handle(final xcb_key_release_event_t event) {
        final X11Seat x11Seat = (X11Seat) this.wlSeat.getSeat()
                                                     .getPlatformImplementation();
        x11Seat.deliverKey(this.wlSeat,
                           event.detail,
                           false);
    }

    @Subscribe
    public void handle(final xcb_button_release_event_t event) {
        final X11Seat x11Seat = (X11Seat) this.wlSeat.getSeat()
                                                     .getPlatformImplementation();
        x11Seat.deliverButton(this.wlSeat,
                              event.time,
                              event.detail,
                              false);
    }

    @Subscribe
    public void handle(final xcb_motion_notify_event_t event) {
        final X11Seat x11Seat = (X11Seat) this.wlSeat.getSeat()
                                                     .getPlatformImplementation();
        x11Seat.deliverMotion(this.wlSeat,
                              event.event_x,
                              event.event_y);
    }

    @Subscribe
    public void handle(final xcb_keymap_notify_event_t event) {
        final X11Seat x11Seat = (X11Seat) this.wlSeat.getSeat()
                                                     .getPlatformImplementation();
        final WlKeyboard     wlKeyboard     = this.wlSeat.getWlKeyboard();
        final KeyboardDevice keyboardDevice = wlKeyboard.getKeyboardDevice();
        final Xkb xkb = this.x11XkbFactory.create(x11Seat.getX11Output()
                                                         .getXcbConnection());
        keyboardDevice.setXkb(xkb);
        keyboardDevice.updateKeymap();
        keyboardDevice.emitKeymap(wlKeyboard.getResources());
    }
}
