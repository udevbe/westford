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
package org.westford.compositor.launch.drm.direct;

import org.freedesktop.jaccall.Pointer;
import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.EventSource;
import org.freedesktop.wayland.server.WlKeyboardResource;
import org.westford.compositor.core.KeyBindingFactory;
import org.westford.compositor.core.KeyboardDevice;
import org.westford.compositor.core.LifeCycle;
import org.westford.compositor.core.PointerDevice;
import org.westford.compositor.core.SurfaceView;
import org.westford.compositor.core.TouchDevice;
import org.westford.compositor.core.events.Activate;
import org.westford.compositor.core.events.Deactivate;
import org.westford.compositor.protocol.WlKeyboard;
import org.westford.compositor.protocol.WlSeat;
import org.westford.launch.LifeCycleSignals;
import org.westford.nativ.glibc.Libc;
import org.westford.nativ.linux.InputEventCodes;
import org.westford.nativ.linux.vt_mode;
import org.westford.tty.Tty;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import static org.westford.nativ.linux.Vt.VT_PROCESS;
import static org.westford.nativ.linux.Vt.VT_SETMODE;

public class Launcher {

    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public static void main_from_native() {
        try {
            //this main is not launched directly, but instead from native code that invokes the jvm after
            //it has properly configured OS signal masks, used for tty switching.

            configureLogger();
            LOGGER.info("Starting Westford");

            new Launcher().launch(DaggerDirectDrmEglCompositor.create());
        }
        catch (final Throwable t) {
            LOGGER.throwing(Launcher.class.getName(),
                            "main_from_native",
                            t);
            t.printStackTrace();
        }
    }

    private static void configureLogger() throws IOException {
        final FileHandler fileHandler = new FileHandler("westford.log");
        fileHandler.setFormatter(new SimpleFormatter());
        LOGGER.addHandler(fileHandler);

        Thread.setDefaultUncaughtExceptionHandler((thread,
                                                   throwable) -> {
            LOGGER.severe("Got uncaught exception " + throwable.getMessage());
            throwable.printStackTrace();
        });
    }

    private void launch(final DirectDrmEglCompositor drmEglCompositor) {

        final LifeCycle lifeCycle = drmEglCompositor.lifeCycle();

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
                                                               pointerDevice.getFocus()
                                                                            .map(SurfaceView::getWlSurfaceResource)));
        touchDevice.getTouchDownSignal()
                   .connect(event -> keyboardDevice.setFocus(wlKeyboardResources,
                                                             touchDevice.getGrab()
                                                                        .map(SurfaceView::getWlSurfaceResource)));

        /*
         * setup tty switching key bindings
         */
        setupTtySwitching(drmEglCompositor,
                          keyboardDevice);

        /*
         * and finally, start the compositor
         */
        lifeCycle.start();
    }

    private void setupTtySwitching(final DirectDrmEglCompositor drmEglCompositor,
                                   final KeyboardDevice keyboardDevice) {
        final Tty  tty  = drmEglCompositor.tty();
        final Libc libc = drmEglCompositor.libc();

       /*
        * SIGRTMIN is used as global VT-acquire+release signal. Note that
        * SIGRT* must be tested on runtime, as their exact values are not
        * known at compile-time. POSIX requires 32 of them to be available.
        */
        if (libc.SIGRTMIN() > libc.SIGRTMAX() ||
            libc.SIGRTMIN() + 1 > libc.SIGRTMAX()) {
            throw new RuntimeException(String.format("not enough RT signals available: %d-%d\n",
                                                     libc.SIGRTMIN(),
                                                     libc.SIGRTMAX()));
        }

        final vt_mode mode = new vt_mode();
        mode.mode(VT_PROCESS);
        mode.relsig((short) libc.SIGRTMIN());
        mode.acqsig((short) libc.SIGRTMIN());
        mode.waitv((byte) 0);
        mode.frsig((byte) 0);
        if (-1 == libc.ioctl(tty.getTtyFd(),
                             VT_SETMODE,
                             Pointer.ref(mode).address)) {
            throw new RuntimeException("Failed to take control of vt handling: " + libc.getStrError());
        }

        final Display          display          = drmEglCompositor.display();
        final LifeCycleSignals lifeCycleSignals = drmEglCompositor.lifeCycleSignals();
        tty.getVtEnterSignal()
           .connect(event -> lifeCycleSignals.getActivateSignal()
                                             .emit(Activate.create()));
        tty.getVtLeaveSignal()
           .connect(event -> lifeCycleSignals.getDeactivateSignal()
                                             .emit(Deactivate.create()));

        final EventSource vtSource = display.getEventLoop()
                                            .addSignal(libc.SIGRTMIN(),
                                                       tty::handleVtSignal);
        lifeCycleSignals.getStopSignal()
                        .connect(event -> vtSource.remove());

        addTtyKeyBindings(drmEglCompositor,
                          keyboardDevice,
                          tty);
    }

    private void addTtyKeyBindings(final DirectDrmEglCompositor drmEglCompositor,
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