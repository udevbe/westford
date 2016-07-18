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

import org.freedesktop.wayland.server.WlKeyboardResource;
import org.westmalle.wayland.bootstrap.dispmanx.DaggerDispmanxEglCompositor;
import org.westmalle.wayland.bootstrap.dispmanx.DispmanxEglCompositor;
import org.westmalle.wayland.bootstrap.drm.DaggerDrmEglCompositor;
import org.westmalle.wayland.bootstrap.drm.DrmEglCompositor;
import org.westmalle.wayland.bootstrap.x11.DaggerX11EglCompositor;
import org.westmalle.wayland.bootstrap.x11.X11EglCompositor;
import org.westmalle.wayland.bootstrap.x11.X11PlatformConfigSimple;
import org.westmalle.wayland.core.KeyboardDevice;
import org.westmalle.wayland.core.LifeCycle;
import org.westmalle.wayland.core.PointerDevice;
import org.westmalle.wayland.core.TouchDevice;
import org.westmalle.wayland.protocol.WlKeyboard;
import org.westmalle.wayland.protocol.WlSeat;
import org.westmalle.wayland.x11.X11PlatformModule;

import java.util.Set;
import java.util.logging.Logger;

public class Boot {

    private static final Logger LOGGER   = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private static final String BACK_END = "BACK_END";


    public static void main(final String[] args) {

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

            default:
                //TODO if wayland display -> wayland else if X display -> x11 else if nothing -> kms
                boot.strap(DaggerX11EglCompositor.builder());
        }
    }

    private void strap(final DaggerDrmEglCompositor.Builder builder) {

        final DrmEglCompositor drmEglCompositor = builder.build();

        /*
         * Keep this first as weston demo clients *really* like their globals
         * to be initialized in a certain order, else they segfault...
         */
        final LifeCycle lifeCycle = drmEglCompositor.lifeCycle();

        final WlSeat wlSeat = drmEglCompositor.seatFactory()
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
        //start the compositor
        lifeCycle.start();
    }
}