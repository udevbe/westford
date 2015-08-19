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
import org.freedesktop.wayland.shared.WlKeyboardKeymapFormat;
import org.freedesktop.wayland.shared.WlSeatCapability;
import org.westmalle.wayland.core.Compositor;
import org.westmalle.wayland.core.KeyboardDeviceFactory;
import org.westmalle.wayland.core.Keymap;
import org.westmalle.wayland.core.PointerDeviceFactory;
import org.westmalle.wayland.core.SeatFactory;
import org.westmalle.wayland.core.Xkb;
import org.westmalle.wayland.core.events.PointerFocus;
import org.westmalle.wayland.nativ.libxcb.Libxcb;
import org.westmalle.wayland.protocol.WlKeyboard;
import org.westmalle.wayland.protocol.WlKeyboardFactory;
import org.westmalle.wayland.protocol.WlOutput;
import org.westmalle.wayland.protocol.WlPointer;
import org.westmalle.wayland.protocol.WlPointerFactory;
import org.westmalle.wayland.protocol.WlSeat;
import org.westmalle.wayland.protocol.WlSeatFactory;
import org.westmalle.wayland.protocol.WlTouchFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.EnumSet;
import java.util.Optional;

public class X11SeatFactory {

    @Nonnull
    private final Libxcb libxcb;

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

    //TODO this class has too many dependencies. See if we can lower this.
    @Inject
    X11SeatFactory(@Nonnull final Libxcb libxcb,
                   @Nonnull final X11XkbFactory x11XkbFactory,
                   @Nonnull final X11InputEventListenerFactory x11InputEventListenerFactory,
                   @Nonnull final WlSeatFactory wlSeatFactory,
                   @Nonnull final SeatFactory seatFactory,
                   @Nonnull final WlPointerFactory wlPointerFactory,
                   @Nonnull final WlKeyboardFactory wlKeyboardFactory,
                   @Nonnull final WlTouchFactory wlTouchFactory,
                   @Nonnull final PointerDeviceFactory pointerDeviceFactory,
                   @Nonnull final KeyboardDeviceFactory keyboardDeviceFactory) {
        this.libxcb = libxcb;
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

        final X11Seat x11Seat = new X11Seat(this.libxcb,
                                            x11Output);

        final Xkb        xkb        = this.x11XkbFactory.create(x11Output.getXcbConnection());
        final WlPointer  wlPointer  = this.wlPointerFactory.create(this.pointerDeviceFactory.create(compositor));
        final WlKeyboard wlKeyboard = this.wlKeyboardFactory.create(this.keyboardDeviceFactory.create(compositor));

        final WlSeat wlSeat = this.wlSeatFactory.create(this.seatFactory.create(xkb,
                                                                                x11Seat),
                                                        wlPointer,
                                                        wlKeyboard,
                                                        this.wlTouchFactory.create());

        x11Output.getX11EventBus()
                 .register(this.x11InputEventListenerFactory.create(wlSeat,
                                                                    x11Seat));
        enableInputDevices(wlSeat);
        addKeyboardFocus(wlPointer,
                         wlKeyboard);
        updateKeymap(wlKeyboard,
                     xkb.getKeymapString());

        return wlSeat;
    }

    private void updateKeymap(final WlKeyboard wlKeyboard,
                              final String keymap) {
        wlKeyboard.getKeyboardDevice()
                  .updateKeymap(wlKeyboard.getResources(),
                                Optional.of(Keymap.create(WlKeyboardKeymapFormat.XKB_V1,
                                                          keymap)));
    }

    private void enableInputDevices(final WlSeat wlSeat) {
        //FIXME for now we put these here, these should be handled dynamically when a mouse or keyboard is
        //added or removed
        //enable pointer and keyboard for wlseat
        wlSeat.getSeat()
              .setCapabilities(EnumSet.of(WlSeatCapability.KEYBOARD,
                                          WlSeatCapability.POINTER));
    }

    private void addKeyboardFocus(final WlPointer wlPointer,
                                  final WlKeyboard wlKeyboard) {
        //FIXME for now we use the pointer focus to set the keyboard focus. Ideally this should be something
        //configurable or implemented by a 3rd party
        wlPointer.getPointerDevice()
                 .register(new Object() {
                     @Subscribe
                     public void handle(final PointerFocus event) {
                         wlKeyboard.getKeyboardDevice()
                                   .setFocus(wlKeyboard
                                                     .getResources(),
                                             event.getWlSurfaceResource());
                     }
                 });
    }
}
