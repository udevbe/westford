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

import org.freedesktop.wayland.shared.WlSeatCapability;
import org.westmalle.wayland.core.KeyboardDevice;
import org.westmalle.wayland.core.KeyboardDeviceFactory;
import org.westmalle.wayland.protocol.WlKeyboardFactory;
import org.westmalle.wayland.protocol.WlSeat;
import org.westmalle.wayland.protocol.WlSeatFactory;

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
    private final WlKeyboardFactory            wlKeyboardFactory;

    @Nonnull
    private final KeyboardDeviceFactory keyboardDeviceFactory;
    @Nonnull
    private final PrivateX11SeatFactory privateX11SeatFactory;

    @Inject
    X11SeatFactory(@Nonnull final PrivateX11SeatFactory privateX11SeatFactory,
                   @Nonnull final X11Platform x11Platform,
                   @Nonnull final X11XkbFactory x11XkbFactory,
                   @Nonnull final X11InputEventListenerFactory x11InputEventListenerFactory,
                   @Nonnull final WlSeatFactory wlSeatFactory,
                   @Nonnull final WlKeyboardFactory wlKeyboardFactory,
                   @Nonnull final KeyboardDeviceFactory keyboardDeviceFactory) {
        this.privateX11SeatFactory = privateX11SeatFactory;
        this.x11Platform = x11Platform;
        this.x11XkbFactory = x11XkbFactory;
        this.x11InputEventListenerFactory = x11InputEventListenerFactory;
        this.wlSeatFactory = wlSeatFactory;
        this.wlKeyboardFactory = wlKeyboardFactory;
        this.keyboardDeviceFactory = keyboardDeviceFactory;
    }

    public WlSeat create() {

        final KeyboardDevice keyboardDevice = this.keyboardDeviceFactory.create(this.x11XkbFactory.create(x11Platform.getXcbConnection()));
        keyboardDevice.updateKeymap();
        final WlSeat wlSeat = this.wlSeatFactory.create(this.wlKeyboardFactory.create(keyboardDevice));

        x11Platform.getX11EventBus()
                   .getXEventSignal()
                   .connect(this.x11InputEventListenerFactory.create(this.privateX11SeatFactory.create(x11Platform,
                                                                                                       wlSeat)));
        //enable pointer and keyboard for wlseat as an X11 seat always has these.
        wlSeat.getSeat()
              .setCapabilities(EnumSet.of(WlSeatCapability.KEYBOARD,
                                          WlSeatCapability.POINTER));

        return wlSeat;
    }
}
