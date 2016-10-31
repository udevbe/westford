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
package org.westford.compositor.launch.x11.html5;

import org.westford.compositor.core.LifeCycle;
import org.westford.compositor.core.PointerDevice;
import org.westford.compositor.protocol.WlKeyboard;
import org.westford.compositor.protocol.WlSeat;
import org.westford.compositor.x11.X11PlatformModule;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Launcher {

    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public static void main(final String[] args) throws IOException, InterruptedException {
        configureLogger();
        LOGGER.info("Starting Westford");

        new Launcher().launch(DaggerHtml5X11EglCompositor.builder());
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

    private void launch(final DaggerHtml5X11EglCompositor.Builder builder) {
        /*
         * Create an X11 compositor with X11 config and wrap it in a html5 compositor.
         */
        final Html5X11EglCompositor html5X11EglCompositor = builder.x11PlatformModule(new X11PlatformModule(new X11PlatformConfigSimple()))
                                                                   .build();

        /*
         * Keep this first as weston demo clients *really* like their globals
         * to be initialized in a certain order, else they segfault...
         */
        final LifeCycle lifeCycle = html5X11EglCompositor.lifeCycle();

        /*
         * Get the X11 seat that listens for input on the X connection and passes it on to a wayland seat.
         * Additional html5 seats will be created dynamically when a remote client connects.
         */
        final WlSeat wlSeat = html5X11EglCompositor.wlSeat();

        /*
         * Setup keyboard focus tracking to follow mouse pointer.
         */
        final WlKeyboard wlKeyboard = wlSeat.getWlKeyboard();
        final PointerDevice pointerDevice = wlSeat.getWlPointer()
                                                  .getPointerDevice();
        pointerDevice.getPointerFocusSignal()
                     .connect(event -> wlKeyboard.getKeyboardDevice()
                                                 .setFocus(wlKeyboard.getResources(),
                                                           pointerDevice.getFocus()));

        lifeCycle.start();
    }
}