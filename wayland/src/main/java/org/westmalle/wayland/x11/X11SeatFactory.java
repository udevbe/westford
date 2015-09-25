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
import org.westmalle.wayland.core.Compositor;
import org.westmalle.wayland.core.KeyboardDevice;
import org.westmalle.wayland.core.KeyboardDeviceFactory;
import org.westmalle.wayland.core.PointerDeviceFactory;
import org.westmalle.wayland.core.SeatFactory;
import org.westmalle.wayland.protocol.WlKeyboardFactory;
import org.westmalle.wayland.protocol.WlOutput;
import org.westmalle.wayland.protocol.WlPointerFactory;
import org.westmalle.wayland.protocol.WlSeat;
import org.westmalle.wayland.protocol.WlSeatFactory;
import org.westmalle.wayland.protocol.WlTouchFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.EnumSet;

public class X11SeatFactory {

    @Nonnull
    private final X11XkbFactory                x11XkbFactory;
    @Nonnull
    private final X11InputEventListenerFactory x11InputEventListenerFactory;
    @Nonnull
    private final WlSeatFactory                wlSeatFactory;
    @Nonnull
    private final SeatFactory                  seatFactory;
    @Nonnull
    private final WlPointerFactory             wlPointerFactory;
    @Nonnull
    private final WlKeyboardFactory            wlKeyboardFactory;

    @Nonnull
    private final WlTouchFactory        wlTouchFactory;
    @Nonnull
    private final PointerDeviceFactory  pointerDeviceFactory;
    @Nonnull
    private final KeyboardDeviceFactory keyboardDeviceFactory;
    @Nonnull
    private final PrivateX11SeatFactory privateX11SeatFactory;

    @Inject
    X11SeatFactory(@Nonnull final PrivateX11SeatFactory privateX11SeatFactory,
                   @Nonnull final X11XkbFactory x11XkbFactory,
                   @Nonnull final X11InputEventListenerFactory x11InputEventListenerFactory,
                   @Nonnull final WlSeatFactory wlSeatFactory,
                   @Nonnull final SeatFactory seatFactory,
                   @Nonnull final WlPointerFactory wlPointerFactory,
                   @Nonnull final WlKeyboardFactory wlKeyboardFactory,
                   @Nonnull final WlTouchFactory wlTouchFactory,
                   @Nonnull final PointerDeviceFactory pointerDeviceFactory,
                   @Nonnull final KeyboardDeviceFactory keyboardDeviceFactory) {
        this.privateX11SeatFactory = privateX11SeatFactory;
        this.x11XkbFactory = x11XkbFactory;
        this.x11InputEventListenerFactory = x11InputEventListenerFactory;
        this.wlSeatFactory = wlSeatFactory;
        this.seatFactory = seatFactory;
        this.wlPointerFactory = wlPointerFactory;
        this.wlKeyboardFactory = wlKeyboardFactory;
        this.wlTouchFactory = wlTouchFactory;
        this.pointerDeviceFactory = pointerDeviceFactory;
        this.keyboardDeviceFactory = keyboardDeviceFactory;
    }

    public WlSeat create(@Nonnull final WlOutput wlOutput,
                         @Nonnull final Compositor compositor) {

        final X11Output x11Output = (X11Output) wlOutput.getOutput()
                                                        .getPlatformImplementation();

        final X11Seat x11Seat = this.privateX11SeatFactory.create(x11Output);

        final KeyboardDevice keyboardDevice = this.keyboardDeviceFactory.create(compositor,
                                                                                this.x11XkbFactory.create(x11Output.getXcbConnection()));
        keyboardDevice.updateKeymap();
        final WlSeat wlSeat = this.wlSeatFactory.create(this.seatFactory.create(x11Seat),
                                                        this.wlPointerFactory.create(this.pointerDeviceFactory.create(compositor)),
                                                        this.wlKeyboardFactory.create(keyboardDevice),
                                                        this.wlTouchFactory.create());
        x11Output.getX11EventBus()
                 .getXEventSignal()
                 .connect(this.x11InputEventListenerFactory.create(wlSeat));
        //enable pointer and keyboard for wlseat as an X11 seat always has these.
        wlSeat.getSeat()
              .setCapabilities(EnumSet.of(WlSeatCapability.KEYBOARD,
                                          WlSeatCapability.POINTER));

        return wlSeat;
    }
}
