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

import org.westmalle.wayland.core.Compositor;
import org.westmalle.wayland.core.KeyboardFactory;
import org.westmalle.wayland.core.PointerDeviceFactory;
import org.westmalle.wayland.nativ.Libxcb;
import org.westmalle.wayland.protocol.WlKeyboardFactory;
import org.westmalle.wayland.protocol.WlOutput;
import org.westmalle.wayland.protocol.WlPointerFactory;
import org.westmalle.wayland.protocol.WlSeat;

import javax.annotation.Nonnull;
import javax.inject.Inject;

public class X11SeatFactory {

    @Nonnull
    private final Libxcb               libxcb;
    @Nonnull
    private final WlPointerFactory     wlPointerFactory;
    @Nonnull
    private final WlKeyboardFactory    wlKeyboardFactory;
    @Nonnull
    private final PointerDeviceFactory pointerDeviceFactory;
    @Nonnull
    private final KeyboardFactory      keyboardFactory;

    @Inject
    X11SeatFactory(@Nonnull final Libxcb libxcb,
                   @Nonnull final WlPointerFactory wlPointerFactory,
                   @Nonnull final WlKeyboardFactory wlKeyboardFactory,
                   @Nonnull final PointerDeviceFactory pointerDeviceFactory,
                   @Nonnull final KeyboardFactory keyboardFactory) {
        this.libxcb = libxcb;
        this.wlPointerFactory = wlPointerFactory;
        this.wlKeyboardFactory = wlKeyboardFactory;
        this.pointerDeviceFactory = pointerDeviceFactory;
        this.keyboardFactory = keyboardFactory;
    }

    public X11Seat create(@Nonnull final WlOutput wlOutput,
                          @Nonnull final WlSeat wlSeat,
                          @Nonnull final Compositor compositor) {

        final X11Output x11Output = (X11Output) wlOutput.getOutput()
                                                        .getImplementation();
        final X11Seat x11Seat = new X11Seat(this.libxcb,
                                            x11Output,
                                            compositor,
                                            wlSeat);
        x11Output.getX11EventBus()
                 .register(x11Seat);
        //FIXME for now we put these here, these should be handled dynamically when a mouse or keyboard is
        //added or removed
        //enable pointer and keyboard for wlseat
        wlSeat.setWlPointer(this.wlPointerFactory.create(this.pointerDeviceFactory.create(compositor)));
        wlSeat.setWlKeyboard(this.wlKeyboardFactory.create(this.keyboardFactory.create()));

        return x11Seat;
    }
}
