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
package org.westmalle.wayland.bootstrap.drm;

import org.freedesktop.wayland.server.WlKeyboardResource;
import org.westmalle.wayland.core.KeyBindingFactory;
import org.westmalle.wayland.core.KeyboardDevice;
import org.westmalle.wayland.core.LifeCycle;
import org.westmalle.wayland.core.PointerDevice;
import org.westmalle.wayland.core.TouchDevice;
import org.westmalle.wayland.nativ.linux.InputEventCodes;
import org.westmalle.wayland.protocol.WlKeyboard;
import org.westmalle.wayland.protocol.WlSeat;
import org.westmalle.wayland.tty.Tty;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class DrmBoot {

    public static void main(final String[] args) {
        new DrmBoot().strap();
    }

    private void strap() {
        final DaggerDrmEglCompositor.Builder builder          = DaggerDrmEglCompositor.builder();
        final DrmEglCompositor               drmEglCompositor = builder.build();

        /*
         * Make sure we initialize the tty before anything else.
         * Keep lifecycle at top as weston demo clients *really* like their globals
         * to be initialized in a certain order, else they segfault...
         */
        final Tty       tty       = drmEglCompositor.tty();
        final LifeCycle lifeCycle = drmEglCompositor.lifeCycle();

        /*
         * Make sure we cleanup nicely if the program stops.
         */
        final Thread shutdownHook = new Thread() {
            @Override
            public void run() {
                tty.close();
            }
        };
        Runtime.getRuntime()
               .addShutdownHook(shutdownHook);

        /*
         * Create a libinput seat that will listen for native input events on seat0.
         */
        final WlSeat wlSeat = drmEglCompositor.seatFactory()
                                              .create("seat0",
                                                      "",
                                                      "",
                                                      "",
                                                      "",
                                                      "");

        /*
         * Setup keyboard focus tracking to follow mouse pointer & touch
         */
        final PointerDevice pointerDevice = wlSeat.getWlPointer()
                                                  .getPointerDevice();
        final TouchDevice touchDevice = wlSeat.getWlTouch()
                                              .getTouchDevice();

        final WlKeyboard              wlKeyboard          = wlSeat.getWlKeyboard();
        final KeyboardDevice          keyboardDevice      = wlKeyboard.getKeyboardDevice();
        final Set<WlKeyboardResource> wlKeyboardResources = wlKeyboard.getResources();

        pointerDevice.getPointerFocusSignal()
                     .connect(event -> keyboardDevice.setFocus(wlKeyboardResources,
                                                               pointerDevice.getFocus()));
        touchDevice.getTouchDownSignal()
                   .connect(event -> keyboardDevice.setFocus(wlKeyboardResources,
                                                             touchDevice.getGrab()));

        /*
         * setup tty switching key bindings
         */
        addTtyKeyBindings(drmEglCompositor,
                          keyboardDevice,
                          tty);

        /*
         * and finally, start the compositor
         */
        lifeCycle.start();
    }

    private void addTtyKeyBindings(final DrmEglCompositor drmEglCompositor,
                                   final KeyboardDevice keyboardDevice,
                                   final Tty tty) {
        final KeyBindingFactory keyBindingFactory = drmEglCompositor.keyBindingFactory();

        keyBindingFactory.create(keyboardDevice,
                                 new HashSet<>(Arrays.asList(InputEventCodes.KEY_LEFTCTRL,
                                                             InputEventCodes.KEY_LEFTALT,
                                                             InputEventCodes.KEY_F1)),
                                 () -> tty.activate(1))
                         .enable();
        keyBindingFactory.create(keyboardDevice,
                                 new HashSet<>(Arrays.asList(InputEventCodes.KEY_LEFTCTRL,
                                                             InputEventCodes.KEY_LEFTALT,
                                                             InputEventCodes.KEY_F2)),
                                 () -> tty.activate(2))
                         .enable();
        keyBindingFactory.create(keyboardDevice,
                                 new HashSet<>(Arrays.asList(InputEventCodes.KEY_LEFTCTRL,
                                                             InputEventCodes.KEY_LEFTALT,
                                                             InputEventCodes.KEY_F3)),
                                 () -> tty.activate(3))
                         .enable();
        keyBindingFactory.create(keyboardDevice,
                                 new HashSet<>(Arrays.asList(InputEventCodes.KEY_LEFTCTRL,
                                                             InputEventCodes.KEY_LEFTALT,
                                                             InputEventCodes.KEY_F4)),
                                 () -> tty.activate(4))
                         .enable();
        keyBindingFactory.create(keyboardDevice,
                                 new HashSet<>(Arrays.asList(InputEventCodes.KEY_LEFTCTRL,
                                                             InputEventCodes.KEY_LEFTALT,
                                                             InputEventCodes.KEY_F5)),
                                 () -> tty.activate(5))
                         .enable();
        keyBindingFactory.create(keyboardDevice,
                                 new HashSet<>(Arrays.asList(InputEventCodes.KEY_LEFTCTRL,
                                                             InputEventCodes.KEY_LEFTALT,
                                                             InputEventCodes.KEY_F6)),
                                 () -> tty.activate(6))
                         .enable();
        keyBindingFactory.create(keyboardDevice,
                                 new HashSet<>(Arrays.asList(InputEventCodes.KEY_LEFTCTRL,
                                                             InputEventCodes.KEY_LEFTALT,
                                                             InputEventCodes.KEY_F7)),
                                 () -> tty.activate(7))
                         .enable();
        keyBindingFactory.create(keyboardDevice,
                                 new HashSet<>(Arrays.asList(InputEventCodes.KEY_LEFTCTRL,
                                                             InputEventCodes.KEY_LEFTALT,
                                                             InputEventCodes.KEY_F8)),
                                 () -> tty.activate(8))
                         .enable();

        keyBindingFactory.create(keyboardDevice,
                                 new HashSet<>(Arrays.asList(InputEventCodes.KEY_LEFTCTRL,
                                                             InputEventCodes.KEY_LEFTALT,
                                                             InputEventCodes.KEY_F9)),
                                 () -> tty.activate(9))
                         .enable();
        keyBindingFactory.create(keyboardDevice,
                                 new HashSet<>(Arrays.asList(InputEventCodes.KEY_LEFTCTRL,
                                                             InputEventCodes.KEY_LEFTALT,
                                                             InputEventCodes.KEY_F10)),
                                 () -> tty.activate(10))
                         .enable();
        keyBindingFactory.create(keyboardDevice,
                                 new HashSet<>(Arrays.asList(InputEventCodes.KEY_LEFTCTRL,
                                                             InputEventCodes.KEY_LEFTALT,
                                                             InputEventCodes.KEY_F11)),
                                 () -> tty.activate(11))
                         .enable();
        keyBindingFactory.create(keyboardDevice,
                                 new HashSet<>(Arrays.asList(InputEventCodes.KEY_LEFTCTRL,
                                                             InputEventCodes.KEY_LEFTALT,
                                                             InputEventCodes.KEY_F12)),
                                 () -> tty.activate(12))
                         .enable();
    }
}
