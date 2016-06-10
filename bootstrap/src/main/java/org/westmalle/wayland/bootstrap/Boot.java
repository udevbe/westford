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

import org.westmalle.wayland.core.LifeCycle;
import org.westmalle.wayland.core.PointerDevice;
import org.westmalle.wayland.dispmanx.DispmanxEglPlatformFactory;
import org.westmalle.wayland.dispmanx.DispmanxPlatform;
import org.westmalle.wayland.dispmanx.DispmanxPlatformFactory;
import org.westmalle.wayland.protocol.WlKeyboard;
import org.westmalle.wayland.protocol.WlSeat;
import org.westmalle.wayland.x11.X11EglPlatformFactory;
import org.westmalle.wayland.x11.X11Platform;
import org.westmalle.wayland.x11.X11PlatformFactory;
import org.westmalle.wayland.x11.X11SeatFactory;

import java.util.logging.Logger;

import static org.westmalle.wayland.nativ.libbcm_host.Libbcm_host.DISPMANX_ID_HDMI;

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
                boot.strap(DaggerX11EglCompositor.create());
                break;

            case "DispmanxEgl":
                boot.strap(DaggerDispmanxEglCompositor.create());
                break;

            default:
                //TODO if wayland display -> wayland else if X display -> x11 else if nothing -> kms
                boot.strap(DaggerX11EglCompositor.create());
        }
    }

    private void strap(final DispmanxEglCompositor dispmanxEglCompositor) {
        /*
         * Keep this first as weston demo clients *really* like their globals
         * to be initialized in a certain order, else they segfault...
         */
        final LifeCycle lifeCycle = dispmanxEglCompositor.lifeCycle();

        //setup dispmanx output back-end.
        final DispmanxPlatformFactory platformFactory = dispmanxEglCompositor.dispmanx()
                                                                             .platformFactory();
        final DispmanxEglPlatformFactory dispmanxEglPlatformFactory = dispmanxEglCompositor.dispmanx()
                                                                                           .eglPlatformFactory();
        //create an output
        //create a dispmanx overlay
        final DispmanxPlatform dispmanxPlatform = platformFactory.create(DISPMANX_ID_HDMI);
        //enable egl
        dispmanxEglPlatformFactory.create(dispmanxPlatform);

        //start the compositor
        lifeCycle.start();
    }

    private void strap(final X11EglCompositor x11EglCompositor) {
        /*
         * Keep this first as weston demo clients *really* like their globals
         * to be initialized in a certain order, else they segfault...
         */
        final LifeCycle lifeCycle = x11EglCompositor.lifeCycle();

        //setup X11 input/output back-end.
        final X11PlatformFactory platformFactory = x11EglCompositor.x11()
                                                                   .platformFactory();
        final X11EglPlatformFactory x11EglPlatformFactory = x11EglCompositor.x11()
                                                                            .eglPlatformFactory();
        final X11SeatFactory seatFactory = x11EglCompositor.x11()
                                                           .seatFactory();
        //create an output
        //create an x11 window
        final X11Platform x11Platform = platformFactory.create(System.getenv("DISPLAY"),
                                                               800,
                                                               600);
        //X egl enabled
        x11EglPlatformFactory.create(x11Platform);

        //setup seat for input support
        //create a seat that listens for input on the X opengl window and passes it on to a wayland seat.
        final WlSeat wlSeat = seatFactory.create(x11Platform);

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