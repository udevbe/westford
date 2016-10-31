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
package org.westford.compositor.x11;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import org.freedesktop.wayland.shared.WlKeyboardKeyState;
import org.freedesktop.wayland.shared.WlPointerAxis;
import org.freedesktop.wayland.shared.WlPointerButtonState;
import org.westford.compositor.core.Point;
import org.westford.compositor.core.PointerDevice;
import org.westford.compositor.protocol.WlKeyboard;
import org.westford.compositor.protocol.WlPointer;
import org.westford.compositor.protocol.WlSeat;
import org.westford.nativ.libxcb.Libxcb;

import javax.annotation.Nonnull;

import static org.freedesktop.wayland.shared.WlPointerAxis.HORIZONTAL_SCROLL;
import static org.freedesktop.wayland.shared.WlPointerAxis.VERTICAL_SCROLL;
import static org.westford.nativ.libxcb.Libxcb.XCB_CURSOR_NONE;
import static org.westford.nativ.libxcb.Libxcb.XCB_EVENT_MASK_BUTTON_PRESS;
import static org.westford.nativ.libxcb.Libxcb.XCB_EVENT_MASK_BUTTON_RELEASE;
import static org.westford.nativ.libxcb.Libxcb.XCB_EVENT_MASK_ENTER_WINDOW;
import static org.westford.nativ.libxcb.Libxcb.XCB_EVENT_MASK_LEAVE_WINDOW;
import static org.westford.nativ.libxcb.Libxcb.XCB_EVENT_MASK_POINTER_MOTION;
import static org.westford.nativ.libxcb.Libxcb.XCB_GRAB_MODE_ASYNC;
import static org.westford.nativ.linux.InputEventCodes.BTN_LEFT;
import static org.westford.nativ.linux.InputEventCodes.BTN_MIDDLE;
import static org.westford.nativ.linux.InputEventCodes.BTN_RIGHT;
import static org.westford.nativ.linux.InputEventCodes.BTN_SIDE;

@AutoFactory(className = "PrivateX11SeatFactory",
             allowSubclasses = true)
public class X11Seat {

    private static final float DEFAULT_AXIS_STEP_DISTANCE = 10.0f;

    @Nonnull
    private final Libxcb      libxcb;
    @Nonnull
    private final X11Platform x11Platform;
    @Nonnull
    private final WlSeat      wlSeat;

    X11Seat(@Provided @Nonnull final Libxcb libxcb,
            @Provided @Nonnull final X11Platform x11Platform,
            @Nonnull final WlSeat wlSeat) {
        this.libxcb = libxcb;
        this.x11Platform = x11Platform;
        this.wlSeat = wlSeat;
    }

    public void deliverKey(final int time,
                           final short eventDetail,
                           final boolean pressed) {
        final WlKeyboardKeyState wlKeyboardKeyState = wlKeyboardKeyState(pressed);
        final int                key                = toLinuxKey(eventDetail);
        final WlKeyboard         wlKeyboard         = this.wlSeat.getWlKeyboard();

        wlKeyboard.getKeyboardDevice()
                  .key(wlKeyboard.getResources(),
                       time,
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
        //convert from X keycodes to input.h keycodes
        return eventDetail - 8;
    }

    public void deliverButton(final int window,
                              final int buttonTime,
                              final short eventDetail,
                              final boolean pressed) {

        final WlPointerButtonState wlPointerButtonState = wlPointerButtonState(window,
                                                                               buttonTime,
                                                                               pressed);
        final int button = toLinuxButton(eventDetail);
        if (button == 0 && pressed) {
            handleScroll(buttonTime,
                         eventDetail);
        }
        else if (button != 0) {
            final WlPointer     wlPointer     = this.wlSeat.getWlPointer();
            final PointerDevice pointerDevice = wlPointer.getPointerDevice();

            pointerDevice.button(wlPointer.getResources(),
                                 buttonTime,
                                 button,
                                 wlPointerButtonState);
            pointerDevice.frame(wlPointer.getResources());
        }
    }

    private WlPointerButtonState wlPointerButtonState(final int window,
                                                      final int buttonTime,
                                                      final boolean pressed) {
        final WlPointerButtonState wlPointerButtonState;
        if (pressed) {
            wlPointerButtonState = WlPointerButtonState.PRESSED;
            this.libxcb.xcb_grab_pointer(this.x11Platform.getXcbConnection(),
                                         (byte) 0,
                                         window,
                                         (short) (XCB_EVENT_MASK_BUTTON_PRESS |
                                                  XCB_EVENT_MASK_BUTTON_RELEASE |
                                                  XCB_EVENT_MASK_POINTER_MOTION |
                                                  XCB_EVENT_MASK_ENTER_WINDOW |
                                                  XCB_EVENT_MASK_LEAVE_WINDOW),
                                         (byte) XCB_GRAB_MODE_ASYNC,
                                         (byte) XCB_GRAB_MODE_ASYNC,
                                         window,
                                         XCB_CURSOR_NONE,
                                         buttonTime);
        }
        else {
            this.libxcb.xcb_ungrab_pointer(this.x11Platform.getXcbConnection(),
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
            case 4:
            case 5:
            case 6:
            case 7:
                //scroll
                button = 0;
                break;
            default:
                button = eventDetail + BTN_SIDE - 8;
        }
        return button;
    }

    private void handleScroll(final int buttonTime,
                              final short eventDetail) {

        final WlPointerAxis wlPointerAxis;
        final float         value;
        final int           discreteValue;

        if (eventDetail == 4 || eventDetail == 5) {
            wlPointerAxis = VERTICAL_SCROLL;
            value = eventDetail == 4 ? -DEFAULT_AXIS_STEP_DISTANCE : DEFAULT_AXIS_STEP_DISTANCE;
            discreteValue = eventDetail == 4 ? -1 : 1;
        }
        else {
            wlPointerAxis = HORIZONTAL_SCROLL;
            value = eventDetail == 6 ? -DEFAULT_AXIS_STEP_DISTANCE : DEFAULT_AXIS_STEP_DISTANCE;
            discreteValue = eventDetail == 6 ? -1 : 1;
        }

        final WlPointer     wlPointer     = this.wlSeat.getWlPointer();
        final PointerDevice pointerDevice = wlPointer.getPointerDevice();

        pointerDevice.axisDiscrete(wlPointer.getResources(),
                                   wlPointerAxis,
                                   buttonTime,
                                   discreteValue,
                                   value);
        pointerDevice.frame(wlPointer.getResources());
    }

    public void deliverMotion(final int windowId,
                              final int time,
                              final int x,
                              final int y) {

        this.x11Platform.getRenderOutputs()
                        .forEach(x11RenderOutput -> {
                            if (x11RenderOutput.getXWindow() == windowId) {

                                final Point point = x11RenderOutput.toGlobal(x,
                                                                             y);

                                final WlPointer     wlPointer     = this.wlSeat.getWlPointer();
                                final PointerDevice pointerDevice = wlPointer.getPointerDevice();

                                pointerDevice.motion(wlPointer.getResources(),
                                                     time,
                                                     point.getX(),
                                                     point.getY());
                                pointerDevice.frame(wlPointer.getResources());
                            }
                        });
    }

    @Nonnull
    public WlSeat getWlSeat() {
        return this.wlSeat;
    }
}
