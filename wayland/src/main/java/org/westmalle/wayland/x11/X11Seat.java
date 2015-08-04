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

import com.google.common.eventbus.Subscribe;
import com.sun.jna.Pointer;
import org.freedesktop.wayland.shared.WlKeyboardKeyState;
import org.freedesktop.wayland.shared.WlKeyboardKeymapFormat;
import org.freedesktop.wayland.shared.WlPointerButtonState;
import org.westmalle.wayland.core.Keymap;
import org.westmalle.wayland.nativ.libxcb.Libxcb;
import org.westmalle.wayland.nativ.libxcb.xcb_button_press_event_t;
import org.westmalle.wayland.nativ.libxcb.xcb_button_release_event_t;
import org.westmalle.wayland.nativ.libxcb.xcb_key_press_event_t;
import org.westmalle.wayland.nativ.libxcb.xcb_key_release_event_t;
import org.westmalle.wayland.nativ.libxcb.xcb_motion_notify_event_t;
import org.westmalle.wayland.nativ.libxkbcommon.Libxkbcommon;
import org.westmalle.wayland.nativ.libxkbcommonx11.Libxkbcommonx11;
import org.westmalle.wayland.protocol.WlKeyboard;
import org.westmalle.wayland.protocol.WlPointer;
import org.westmalle.wayland.protocol.WlSeat;

import javax.annotation.Nonnull;
import java.util.Optional;

import static org.westmalle.wayland.nativ.libxcb.Libxcb.XCB_CURSOR_NONE;
import static org.westmalle.wayland.nativ.libxcb.Libxcb.XCB_EVENT_MASK_BUTTON_PRESS;
import static org.westmalle.wayland.nativ.libxcb.Libxcb.XCB_EVENT_MASK_BUTTON_RELEASE;
import static org.westmalle.wayland.nativ.libxcb.Libxcb.XCB_EVENT_MASK_ENTER_WINDOW;
import static org.westmalle.wayland.nativ.libxcb.Libxcb.XCB_EVENT_MASK_LEAVE_WINDOW;
import static org.westmalle.wayland.nativ.libxcb.Libxcb.XCB_EVENT_MASK_POINTER_MOTION;
import static org.westmalle.wayland.nativ.libxcb.Libxcb.XCB_GRAB_MODE_ASYNC;
import static org.westmalle.wayland.nativ.libxkbcommon.Libxkbcommon.XKB_KEYMAP_COMPILE_NO_FLAGS;
import static org.westmalle.wayland.nativ.libxkbcommon.Libxkbcommon.XKB_KEYMAP_FORMAT_TEXT_V1;
import static org.westmalle.wayland.nativ.linux.Input.BTN_LEFT;
import static org.westmalle.wayland.nativ.linux.Input.BTN_MIDDLE;
import static org.westmalle.wayland.nativ.linux.Input.BTN_RIGHT;

public class X11Seat {

    @Nonnull
    private final Libxcb          libxcb;
    @Nonnull
    private final Libxkbcommon    libxkbcommon;
    @Nonnull
    private final Libxkbcommonx11 libxkbcommonx11;
    @Nonnull
    private final Pointer         xkbContext;
    @Nonnull
    private final X11Output       x11Output;
    @Nonnull
    private final WlSeat          wlSeat;

    X11Seat(@Nonnull final Libxcb libxcb,
            @Nonnull final Libxkbcommon libxkbcommon,
            @Nonnull final Libxkbcommonx11 libxkbcommonx11,
            @Nonnull final Pointer xkbContext,
            @Nonnull final X11Output x11Output,
            @Nonnull final WlSeat wlSeat) {
        this.libxcb = libxcb;
        this.libxkbcommon = libxkbcommon;
        this.libxkbcommonx11 = libxkbcommonx11;
        this.xkbContext = xkbContext;
        this.x11Output = x11Output;
        this.wlSeat = wlSeat;
    }

    @Subscribe
    public void handle(final xcb_key_press_event_t event) {
        deliverKey(event.detail,
                   true);
    }

    private void deliverKey(final short eventDetail,
                            final boolean pressed) {
        final WlKeyboardKeyState wlKeyboardKeyState = wlKeyboardKeyState(pressed);
        final int                key                = toLinuxKey(eventDetail);
        final WlKeyboard         wlKeyboard         = this.wlSeat.getWlKeyboard();
        wlKeyboard.getKeyboardDevice()
                  .key(wlKeyboard.getResources(),
                       key,
                       wlKeyboardKeyState);
    }

    private WlKeyboardKeyState wlKeyboardKeyState(final boolean pressed) {
        final WlKeyboardKeyState wlKeyboardKeyState;
        if (pressed) {
            wlKeyboardKeyState = WlKeyboardKeyState.PRESSED;
        }
        else {
            wlKeyboardKeyState = WlKeyboardKeyState.RELEASED;
        }
        return wlKeyboardKeyState;
    }

    private int toLinuxKey(final short eventDetail) {
        //TODO convert from X keycodes to input.h keycodes, -> properly use xkbcommon
        return eventDetail;
    }

    @Subscribe
    public void handle(final xcb_button_press_event_t event) {
        deliverButton(event.time,
                      event.detail,
                      true);
    }

    private void deliverButton(final int buttonTime,
                               final short eventDetail,
                               final boolean pressed) {

        final WlPointerButtonState wlPointerButtonState = wlPointerButtonState(buttonTime,
                                                                               pressed);
        final int button = toLinuxButton(eventDetail);

        final WlPointer wlPointer = this.wlSeat.getWlPointer();
        wlPointer.getPointerDevice()
                 .button(wlPointer.getResources(),
                         button,
                         wlPointerButtonState);
    }

    private WlPointerButtonState wlPointerButtonState(final int buttonTime,
                                                      final boolean pressed) {
        final WlPointerButtonState wlPointerButtonState;
        if (pressed) {
            wlPointerButtonState = WlPointerButtonState.PRESSED;
            this.libxcb.xcb_grab_pointer(this.x11Output.getXcbConnection(),
                                         (byte) 0,
                                         this.x11Output.getxWindow(),
                                         (short) (XCB_EVENT_MASK_BUTTON_PRESS |
                                                  XCB_EVENT_MASK_BUTTON_RELEASE |
                                                  XCB_EVENT_MASK_POINTER_MOTION |
                                                  XCB_EVENT_MASK_ENTER_WINDOW |
                                                  XCB_EVENT_MASK_LEAVE_WINDOW),
                                         (byte) XCB_GRAB_MODE_ASYNC,
                                         (byte) XCB_GRAB_MODE_ASYNC,
                                         this.x11Output.getxWindow(),
                                         XCB_CURSOR_NONE,
                                         buttonTime);
        }
        else {
            this.libxcb.xcb_ungrab_pointer(this.x11Output.getXcbConnection(),
                                           buttonTime);
            wlPointerButtonState = WlPointerButtonState.RELEASED;
        }
        return wlPointerButtonState;
    }

    private int toLinuxButton(final int eventDetail) {
        final int button;
        switch (eventDetail) {
            case 1:
                button = BTN_LEFT;
                break;
            case 2:
                button = BTN_MIDDLE;
                break;
            case 3:
                button = BTN_RIGHT;
                break;
            default:
                button = 0;
        }
        return button;
    }

    @Subscribe
    public void handle(final xcb_key_release_event_t event) {
        deliverKey(event.detail,
                   false);
    }

    @Subscribe
    public void handle(final xcb_button_release_event_t event) {
        deliverButton(event.time,
                      event.detail,
                      false);
    }

    @Subscribe
    public void handle(final xcb_motion_notify_event_t event) {
        final int x = event.event_x;
        final int y = event.event_y;

        final WlPointer wlPointer = this.wlSeat.getWlPointer();
        wlPointer.getPointerDevice()
                 .motion(wlPointer.getResources(),
                         x,
                         y);
    }

    public void updateKeymap() {

        final Pointer xcbConnection = this.x11Output.getXcbConnection();
        final int     device_id     = this.libxkbcommonx11.xkb_x11_get_core_keyboard_device_id(xcbConnection);
        if (device_id == -1) {
            //TODO error
        }
        final Pointer keymap = this.libxkbcommonx11.xkb_x11_keymap_new_from_device(xkbContext,
                                                                                   xcbConnection,
                                                                                   device_id,
                                                                                   XKB_KEYMAP_COMPILE_NO_FLAGS);
        //FIXME check and handle null
        final Pointer keymapAsStringPointer = this.libxkbcommon.xkb_keymap_get_as_string(keymap,
                                                                                         XKB_KEYMAP_FORMAT_TEXT_V1);

        final WlKeyboard wlKeyboard = this.wlSeat.getWlKeyboard();
        wlKeyboard.getKeyboardDevice()
                  .updateKeymap(wlKeyboard.getResources(),
                                Optional.of(Keymap.create(WlKeyboardKeymapFormat.XKB_V1,
                                                          //FIXME check and handle null
                                                          keymapAsStringPointer.getString(0))));
    }
}
