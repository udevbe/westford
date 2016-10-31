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
package org.westford.compositor.html5;


import org.freedesktop.wayland.shared.WlSeatCapability;
import org.westford.compositor.core.KeyboardDevice;
import org.westford.compositor.core.KeyboardDeviceFactory;
import org.westford.compositor.core.PointerDevice;
import org.westford.compositor.input.LibinputXkbFactory;
import org.westford.compositor.protocol.WlKeyboard;
import org.westford.compositor.protocol.WlKeyboardFactory;
import org.westford.compositor.protocol.WlPointerFactory;
import org.westford.compositor.protocol.WlSeat;
import org.westford.compositor.protocol.WlSeatFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.EnumSet;

public class Html5SeatFactory {

    @Nonnull
    private final PrivateHtml5SeatFactory privateHtml5SeatFactory;
    @Nonnull
    private final WlSeatFactory           wlSeatFactory;
    @Nonnull
    private final WlPointerFactory        wlPointerFactory;
    @Nonnull
    private final WlKeyboardFactory       wlKeyboardFactory;
    @Nonnull
    private final KeyboardDeviceFactory   keyboardDeviceFactory;
    @Nonnull
    private final LibinputXkbFactory      libinputXkbFactory;

    @Inject
    Html5SeatFactory(@Nonnull final PrivateHtml5SeatFactory privateHtml5SeatFactory,
                     @Nonnull final WlSeatFactory wlSeatFactory,
                     @Nonnull final WlPointerFactory wlPointerFactory,
                     @Nonnull final WlKeyboardFactory wlKeyboardFactory,
                     @Nonnull final KeyboardDeviceFactory keyboardDeviceFactory,
                     @Nonnull final LibinputXkbFactory libinputXkbFactory) {
        this.privateHtml5SeatFactory = privateHtml5SeatFactory;
        this.wlSeatFactory = wlSeatFactory;
        this.wlPointerFactory = wlPointerFactory;
        this.wlKeyboardFactory = wlKeyboardFactory;
        this.keyboardDeviceFactory = keyboardDeviceFactory;
        this.libinputXkbFactory = libinputXkbFactory;
    }

    public Html5Seat create() {
        //TODO make this configurable either on server or on client side.
        final String keyboardRule    = "";
        final String keyboardModel   = "";
        final String keyboardLayout  = "";
        final String keyboardVariant = "";
        final String keyboardOptions = "";

        final KeyboardDevice keyboardDevice = this.keyboardDeviceFactory.create(this.libinputXkbFactory.create(keyboardRule,
                                                                                                               keyboardModel,
                                                                                                               keyboardLayout,
                                                                                                               keyboardVariant,
                                                                                                               keyboardOptions));
        keyboardDevice.updateKeymap();
        final WlSeat wlSeat = this.wlSeatFactory.create(this.wlPointerFactory.create(),
                                                        this.wlKeyboardFactory.create(keyboardDevice));
        //enable pointer and keyboard for wlseat as a browser always has these.
        wlSeat.getSeat()
              .setCapabilities(EnumSet.of(WlSeatCapability.KEYBOARD,
                                          WlSeatCapability.POINTER));


        //TODO make this configurable
        //setup keyboard focus tracking to follow mouse pointer
        final WlKeyboard wlKeyboard = wlSeat.getWlKeyboard();
        final PointerDevice pointerDevice = wlSeat.getWlPointer()
                                                  .getPointerDevice();
        pointerDevice.getPointerFocusSignal()
                     .connect(event -> wlKeyboard.getKeyboardDevice()
                                                 .setFocus(wlKeyboard.getResources(),
                                                           pointerDevice.getFocus()));

        return this.privateHtml5SeatFactory.create(wlSeat);
    }
}
