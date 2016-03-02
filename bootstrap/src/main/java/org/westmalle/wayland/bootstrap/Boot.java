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

import com.beust.jcommander.JCommander;
import org.westmalle.wayland.core.LifeCycle;
import org.westmalle.wayland.core.PointerDevice;
import org.westmalle.wayland.dispmanx.DispmanxOutputFactory;
import org.westmalle.wayland.protocol.WlKeyboard;
import org.westmalle.wayland.protocol.WlOutput;
import org.westmalle.wayland.protocol.WlSeat;
import org.westmalle.wayland.x11.X11OutputFactory;
import org.westmalle.wayland.x11.X11SeatFactory;

import java.util.Arrays;
import java.util.logging.Logger;

import static org.westmalle.wayland.nativ.libbcm_host.Libbcm_host.DISPMANX_ID_HDMI;

class Boot {

    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);


    public static void main(final String[] args) {

        //jaccall debug output
//        final Handler consoleHandler = new ConsoleHandler();
//        consoleHandler.setLevel(Level.FINE);
//        final Logger jaccallLogger = Logger.getLogger("jaccall");
//        jaccallLogger.setLevel(Level.FINE);
//        jaccallLogger.addHandler(consoleHandler);

        Thread.setDefaultUncaughtExceptionHandler((thread,
                                                   throwable) -> {
            LOGGER.severe("Got uncaught exception " + throwable.getMessage());
            throwable.printStackTrace();
        });

        LOGGER.info(String.format("Starting Westmalle:\n"
                                  + "\tArguments: %s",
                                  args.length == 0 ? "<none>" : Arrays.toString(args)));

        final CommandBackend commandBackend = parseBackend(args);
        read(commandBackend);
    }

    private static CommandBackend parseBackend(final String[] args) {
        final CommandBackend commandBackend = new CommandBackend();
        new JCommander(commandBackend,
                       args);
        return commandBackend;
    }

    private static void read(final CommandBackend commandBackend) {
        final Boot boot = new Boot();

        switch (commandBackend.backend) {
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
        final DispmanxOutputFactory outputFactory = dispmanxEglCompositor.dispmanx()
                                                                         .outputFactory();
        //create an output
        //create an opengl enabled egl overlay
        outputFactory.create(DISPMANX_ID_HDMI);

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
        final X11OutputFactory outputFactory = x11EglCompositor.x11()
                                                               .outputFactory();
        final X11SeatFactory seatFactory = x11EglCompositor.x11()
                                                           .seatFactory();
        //create an output
        //create an X opengl enabled x11 window
        final WlOutput wlOutput = outputFactory.create(System.getenv("DISPLAY"),
                                                       800,
                                                       600);

        //setup seat for input support
        //create a seat that listens for input on the X opengl window and passes it on to a wayland seat.
        final WlSeat wlSeat = seatFactory.create(wlOutput);

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