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
package org.westmalle.compositor.x11;

import org.freedesktop.wayland.shared.WlSeatCapability;
import org.westmalle.compositor.core.KeyboardDevice;
import org.westmalle.compositor.core.KeyboardDeviceFactory;
import org.westmalle.compositor.core.Xkb;
import org.westmalle.compositor.protocol.WlKeyboardFactory;
import org.westmalle.compositor.protocol.WlPointerFactory;
import org.westmalle.compositor.protocol.WlSeat;
import org.westmalle.compositor.protocol.WlSeatFactory;
import org.westmalle.compositor.x11.PrivateX11SeatFactory;
import org.westmalle.compositor.x11.X11InputEventListenerFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.EnumSet;

public class X11SeatFactory {

    @Nonnull
    private final X11Platform                  x11Platform;
    @Nonnull
    private final X11XkbFactory                x11XkbFactory;
    @Nonnull
    private final X11InputEventListenerFactory x11InputEventListenerFactory;
    @Nonnull
    private final WlSeatFactory                wlSeatFactory;
    @Nonnull
    private final WlPointerFactory             wlPointerFactory;
    @Nonnull
    private final WlKeyboardFactory            wlKeyboardFactory;
    @Nonnull
    private final KeyboardDeviceFactory        keyboardDeviceFactory;
    @Nonnull
    private final PrivateX11SeatFactory        privateX11SeatFactory;

    @Inject
    X11SeatFactory(@Nonnull final PrivateX11SeatFactory privateX11SeatFactory,
                   @Nonnull final X11Platform x11Platform,
                   @Nonnull final X11XkbFactory x11XkbFactory,
                   @Nonnull final X11InputEventListenerFactory x11InputEventListenerFactory,
                   @Nonnull final WlSeatFactory wlSeatFactory,
                   @Nonnull final WlPointerFactory wlPointerFactory,
                   @Nonnull final WlKeyboardFactory wlKeyboardFactory,
                   @Nonnull final KeyboardDeviceFactory keyboardDeviceFactory) {
        this.privateX11SeatFactory = privateX11SeatFactory;
        this.x11Platform = x11Platform;
        this.x11XkbFactory = x11XkbFactory;
        this.x11InputEventListenerFactory = x11InputEventListenerFactory;
        this.wlSeatFactory = wlSeatFactory;
        this.wlPointerFactory = wlPointerFactory;
        this.wlKeyboardFactory = wlKeyboardFactory;
        this.keyboardDeviceFactory = keyboardDeviceFactory;
    }

    public WlSeat create() {

        final long           xcbConnection  = this.x11Platform.getXcbConnection();
        final Xkb            xkb            = this.x11XkbFactory.create(xcbConnection);
        final KeyboardDevice keyboardDevice = this.keyboardDeviceFactory.create(xkb);
        keyboardDevice.updateKeymap();

        final WlSeat wlSeat = this.wlSeatFactory.create(this.wlPointerFactory.create(),
                                                        this.wlKeyboardFactory.create(keyboardDevice));

        this.x11Platform.getX11EventBus()
                        .getXEventSignal()
                        .connect(this.x11InputEventListenerFactory.create(this.privateX11SeatFactory.create(wlSeat)));
        //enable pointer and keyboard for wlseat as an X11 seat always has these.
        wlSeat.getSeat()
              .setCapabilities(EnumSet.of(WlSeatCapability.KEYBOARD,
                                          WlSeatCapability.POINTER));

        return wlSeat;
    }
}
