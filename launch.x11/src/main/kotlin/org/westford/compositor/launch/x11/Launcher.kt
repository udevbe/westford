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
package org.westford.compositor.launch.x11

import org.westford.compositor.x11.X11PlatformModule
import java.io.IOException
import java.util.logging.FileHandler
import java.util.logging.Logger
import java.util.logging.SimpleFormatter

class Launcher {

    private fun launch(builder: DaggerX11EglCompositor.Builder) {

        /*
         * Inject X11 config.
         */
        val x11EglCompositor = builder.x11PlatformModule(X11PlatformModule(X11PlatformConfigSimple())).build()

        /*
         * Keep this first as weston demo clients *really* like their globals
         * to be initialized in a certain order, else they segfault...
         */
        val lifeCycle = x11EglCompositor.lifeCycle()

        /*Get the seat that listens for input on the X connection and passes it on to a wayland seat.
         */
        val wlSeat = x11EglCompositor.wlSeat()

        /*
         * Setup keyboard focus tracking to follow mouse pointer.
         */
        val wlKeyboard = wlSeat.wlKeyboard
        val pointerDevice = wlSeat.wlPointer.pointerDevice
        pointerDevice.pointerFocusSignal.connect {
            wlKeyboard.keyboardDevice.setFocus(wlKeyboard.resources,
                                               pointerDevice.focus.wlSurfaceResource)
        }
        /*
         * Start the compositor.
         */
        lifeCycle.start()
    }

    companion object {

        private val LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME)

        @Throws(IOException::class) @JvmStatic fun main(args: Array<String>) {
            configureLogger()
            LOGGER.info("Starting Westford")

            Launcher().launch(DaggerX11EglCompositor.builder())
        }

        @Throws(IOException::class) private fun configureLogger() {
            val fileHandler = FileHandler("westford.log")
            fileHandler.formatter = SimpleFormatter()
            LOGGER.addHandler(fileHandler)

            Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
                LOGGER.severe("Got uncaught exception " + throwable.message)
                throwable.printStackTrace()
            }
        }
    }
}