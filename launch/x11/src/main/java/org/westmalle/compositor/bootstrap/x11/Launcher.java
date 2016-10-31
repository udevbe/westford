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
package org.westmalle.compositor.bootstrap.x11;

import org.westmalle.compositor.core.LifeCycle;
import org.westmalle.compositor.core.PointerDevice;
import org.westmalle.compositor.protocol.WlKeyboard;
import org.westmalle.compositor.protocol.WlSeat;
import org.westmalle.compositor.x11.X11PlatformModule;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Launcher {

    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public static void main(final String[] args) throws IOException {
        configureLogger();
        LOGGER.info("Starting Westmalle");

        new Launcher().launch(DaggerX11EglCompositor.builder());
    }

    private static void configureLogger() throws IOException {
        final FileHandler fileHandler = new FileHandler("westmalle.log");
        fileHandler.setFormatter(new SimpleFormatter());
        LOGGER.addHandler(fileHandler);

        Thread.setDefaultUncaughtExceptionHandler((thread,
                                                   throwable) -> {
            LOGGER.severe("Got uncaught exception " + throwable.getMessage());
            throwable.printStackTrace();
        });
    }

    private void launch(final DaggerX11EglCompositor.Builder builder) {

        /*
         * Inject X11 config.
         */
        final X11EglCompositor x11EglCompositor = builder.x11PlatformModule(new X11PlatformModule(new X11PlatformConfigSimple()))
                                                         .build();

        /*
         * Keep this first as weston demo clients *really* like their globals
         * to be initialized in a certain order, else they segfault...
         */
        final LifeCycle lifeCycle = x11EglCompositor.lifeCycle();

        /*Get the seat that listens for input on the X connection and passes it on to a wayland seat.
         */
        final WlSeat wlSeat = x11EglCompositor.wlSeat();

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
        /*
         * Start the compositor.
         */
        lifeCycle.start();
    }
}