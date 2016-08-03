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
package org.westmalle.wayland.bootstrap;

import org.freedesktop.jaccall.CLong;
import org.freedesktop.jaccall.Lib;
import org.freedesktop.jaccall.Pointer;
import org.freedesktop.wayland.server.WlKeyboardResource;
import org.westmalle.wayland.bootstrap.dispmanx.DaggerDispmanxEglCompositor;
import org.westmalle.wayland.bootstrap.dispmanx.DispmanxEglCompositor;
import org.westmalle.wayland.bootstrap.drm.DaggerDrmEglCompositor;
import org.westmalle.wayland.bootstrap.drm.DrmEglCompositor;
import org.westmalle.wayland.bootstrap.html5.DaggerHtml5X11EglCompositor;
import org.westmalle.wayland.bootstrap.html5.Html5X11EglCompositor;
import org.westmalle.wayland.bootstrap.x11.DaggerX11EglCompositor;
import org.westmalle.wayland.bootstrap.x11.X11EglCompositor;
import org.westmalle.wayland.bootstrap.x11.X11PlatformConfigSimple;
import org.westmalle.wayland.core.KeyBindingFactory;
import org.westmalle.wayland.core.KeyboardDevice;
import org.westmalle.wayland.core.LifeCycle;
import org.westmalle.wayland.core.PointerDevice;
import org.westmalle.wayland.core.TouchDevice;
import org.westmalle.wayland.nativ.glibc.Libc;
import org.westmalle.wayland.nativ.glibc.Libpthread;
import org.westmalle.wayland.nativ.linux.InputEventCodes;
import org.westmalle.wayland.protocol.WlKeyboard;
import org.westmalle.wayland.protocol.WlSeat;
import org.westmalle.wayland.tty.Tty;
import org.westmalle.wayland.x11.X11PlatformModule;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Boot {

    private static final Logger LOGGER   = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private static final String BACK_END = "backEnd";


    public static void main(final String[] args) throws IOException {

        final FileHandler fileHandler = new FileHandler("westmalle.log");
        fileHandler.setFormatter(new SimpleFormatter());
        LOGGER.addHandler(fileHandler);

        Thread.setDefaultUncaughtExceptionHandler((thread,
                                                   throwable) -> {
            LOGGER.severe("Got uncaught exception " + throwable.getMessage());
            throwable.printStackTrace();
        });

        LOGGER.info("Starting Westmalle");

        initBackEnd();
    }

    private static void initBackEnd() {
        final Boot boot = new Boot();

        String backEnd = System.getProperty(BACK_END);
        if (backEnd == null) {
            backEnd = "";
        }

        switch (backEnd) {
            case "X11Egl":
                boot.strap(DaggerX11EglCompositor.builder());
                break;

            case "DispmanxEgl":
                boot.strap(DaggerDispmanxEglCompositor.builder());
                break;

            case "DrmEgl":
                boot.strap(DaggerDrmEglCompositor.builder());
                break;
            case "Html5X11Egl":
                boot.strap(DaggerHtml5X11EglCompositor.builder());
                break;
            default:
                //TODO if wayland display -> wayland else if X display -> x11 else if nothing -> kms
                System.err.println("No back end specified. Defaulting to 'X11Egl'. Specify your back end with -DbackEnd=<value>.\n" +
                                   "Available back ends:\n" +
                                   "\tX11Egl\n" +
                                   "\tDispmanxEgl\n" +
                                   "\tDrmEgl\n" +
                                   "\tHtml5X11Egl");
                boot.strap(DaggerX11EglCompositor.builder());
        }
    }

    private void strap(final DaggerHtml5X11EglCompositor.Builder builder) {
        final Html5X11EglCompositor html5X11EglCompositor = builder.x11PlatformModule(new X11PlatformModule(new X11PlatformConfigSimple()))
                                                                   .build();
        final LifeCycle lifeCycle = html5X11EglCompositor.lifeCycle();

        //setup seat for input support
        //get the seat that listens for input on the X connection and passes it on to a wayland seat.
        final WlSeat wlSeat = html5X11EglCompositor.wlSeat();

        //setup keyboard focus tracking to follow mouse pointer
        final WlKeyboard wlKeyboard = wlSeat.getWlKeyboard();
        final PointerDevice pointerDevice = wlSeat.getWlPointer()
                                                  .getPointerDevice();
        pointerDevice.getPointerFocusSignal()
                     .connect(event -> wlKeyboard.getKeyboardDevice()
                                                 .setFocus(wlKeyboard.getResources(),
                                                           pointerDevice.getFocus()));

        lifeCycle.start();
    }

    private void strap(final DaggerDrmEglCompositor.Builder builder) {

        final DrmEglCompositor drmEglCompositor = builder.build();

        /*
         * Make sure we initialize the tty before anything else.
         */
        final Tty tty = drmEglCompositor.tty();
        /*
         * Make sure we close the tty if the program exits.
         */
        Runtime.getRuntime()
               .addShutdownHook(new Thread() {
                   @Override
                   public void run() {
                       tty.close();
                   }
               });

        /*
         * Keep this first as weston demo clients *really* like their globals
         * to be initialized in a certain order, else they segfault...
         */
        final LifeCycle lifeCycle = drmEglCompositor.lifeCycle();

        /*
         * Create a libinput seat that will listen for native input events
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

        /*
         * and finally, start the compositor
         */
        lifeCycle.start();
    }

    private void strap(final DaggerDispmanxEglCompositor.Builder builder) {

        final DispmanxEglCompositor dispmanxEglCompositor = builder.build();

        /*
         * Keep this first as weston demo clients *really* like their globals
         * to be initialized in a certain order, else they segfault...
         */
        final LifeCycle lifeCycle = dispmanxEglCompositor.lifeCycle();

        final WlSeat wlSeat = dispmanxEglCompositor.seatFactory()
                                                   .create("seat0",
                                                           "",
                                                           "",
                                                           "",
                                                           "",
                                                           "");

        //setup keyboard focus tracking to follow mouse pointer & touch
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

        //start the compositor
        lifeCycle.start();
    }

    private void strap(final DaggerX11EglCompositor.Builder builder) {

        //inject config
        final X11EglCompositor x11EglCompositor = builder.x11PlatformModule(new X11PlatformModule(new X11PlatformConfigSimple()))
                                                         .build();

        /*
         * Keep this first as weston demo clients *really* like their globals
         * to be initialized in a certain order, else they segfault...
         */
        final LifeCycle lifeCycle = x11EglCompositor.lifeCycle();

        //setup seat for input support
        //get the seat that listens for input on the X connection and passes it on to a wayland seat.
        final WlSeat wlSeat = x11EglCompositor.wlSeat();

        //setup keyboard focus tracking to follow mouse pointer
        final WlKeyboard wlKeyboard = wlSeat.getWlKeyboard();
        final PointerDevice pointerDevice = wlSeat.getWlPointer()
                                                  .getPointerDevice();
        pointerDevice.getPointerFocusSignal()
                     .connect(event -> wlKeyboard.getKeyboardDevice()
                                                 .setFocus(wlKeyboard.getResources(),
                                                           pointerDevice.getFocus()));

        final Libc       libc       = x11EglCompositor.libc();
        final Libpthread libpthread = x11EglCompositor.libpthread();
        Pointer<Byte>    sigset     = Pointer.nref(new byte[128]);
        libpthread.sigemptyset(sigset.address);
        libpthread.sigaddset(sigset.address,
                             libc.SIGRTMAX());
        libpthread.pthread_sigmask(Libc.SIG_UNBLOCK,
                                   sigset.address,
                                   0L);

            x11EglCompositor.display()
                            .getEventLoop()
                            .addSignal(libc.SIGRTMAX(),
                                       signalNumber -> {
                                           System.out.println("signal received: " + signalNumber);
                                           return 0;
                                       });


        //start the compositor
        lifeCycle.start();
    }
}