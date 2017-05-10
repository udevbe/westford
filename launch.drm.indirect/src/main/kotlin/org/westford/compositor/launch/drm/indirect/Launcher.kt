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
package org.westford.compositor.launch.drm.indirect

import org.westford.compositor.core.KeyboardDevice
import org.westford.nativ.linux.InputEventCodes
import org.westford.tty.Tty
import java.io.IOException
import java.util.logging.FileHandler
import java.util.logging.Logger
import java.util.logging.SimpleFormatter

class Launcher {

    fun launch(drmEglCompositor: IndirectDrmEglCompositor) {

        val lifeCycle = drmEglCompositor.lifeCycle()

        /*
         * Create a libinput seat that will listen for native input events on seat0.
         */
        val wlSeat = drmEglCompositor.seatFactory().create("seat0",
                                                           "",
                                                           "",
                                                           "",
                                                           "",
                                                           "")

        /*
         * Setup keyboard focus tracking to follow mouse pointer & touch
         */
        val pointerDevice = wlSeat.wlPointer.pointerDevice
        val touchDevice = wlSeat.wlTouch.touchDevice

        val wlKeyboard = wlSeat.wlKeyboard
        val keyboardDevice = wlKeyboard.keyboardDevice
        val wlKeyboardResources = wlKeyboard.resources

        pointerDevice.pointerFocusSignal.connect {
            keyboardDevice.setFocus(wlKeyboardResources,
                                    pointerDevice.focus?.wlSurfaceResource)
        }
        touchDevice.touchDownSignal.connect {
            keyboardDevice.setFocus(wlKeyboardResources,
                                    touchDevice.grab?.wlSurfaceResource)
        }

        /*
         * setup tty switching key bindings
         */
        val tty = drmEglCompositor.tty()
        addTtyKeyBindings(drmEglCompositor,
                          keyboardDevice,
                          tty)

        /*
         * and finally, start the compositor
         */
        lifeCycle.start()
    }

    private fun addTtyKeyBindings(drmEglCompositor: IndirectDrmEglCompositor,
                                  keyboardDevice: KeyboardDevice,
                                  tty: Tty) {

        val keyBindingFactory = drmEglCompositor.keyBindingFactory()

        keyBindingFactory.create(keyboardDevice,
                                 setOf(InputEventCodes.KEY_LEFTCTRL,
                                       InputEventCodes.KEY_LEFTALT,
                                       InputEventCodes.KEY_F1)) { tty.activate(1) }.enable()
        keyBindingFactory.create(keyboardDevice,
                                 setOf(InputEventCodes.KEY_LEFTCTRL,
                                       InputEventCodes.KEY_LEFTALT,
                                       InputEventCodes.KEY_F2)) { tty.activate(2) }.enable()
        keyBindingFactory.create(keyboardDevice,
                                 setOf(InputEventCodes.KEY_LEFTCTRL,
                                       InputEventCodes.KEY_LEFTALT,
                                       InputEventCodes.KEY_F3)) { tty.activate(3) }.enable()
        keyBindingFactory.create(keyboardDevice,
                                 setOf(InputEventCodes.KEY_LEFTCTRL,
                                       InputEventCodes.KEY_LEFTALT,
                                       InputEventCodes.KEY_F4)) { tty.activate(4) }.enable()
        keyBindingFactory.create(keyboardDevice,
                                 setOf(InputEventCodes.KEY_LEFTCTRL,
                                       InputEventCodes.KEY_LEFTALT,
                                       InputEventCodes.KEY_F5)) { tty.activate(5) }.enable()
        keyBindingFactory.create(keyboardDevice,
                                 setOf(InputEventCodes.KEY_LEFTCTRL,
                                       InputEventCodes.KEY_LEFTALT,
                                       InputEventCodes.KEY_F6)) { tty.activate(6) }.enable()
        keyBindingFactory.create(keyboardDevice,
                                 setOf(InputEventCodes.KEY_LEFTCTRL,
                                       InputEventCodes.KEY_LEFTALT,
                                       InputEventCodes.KEY_F7)) { tty.activate(7) }.enable()
        keyBindingFactory.create(keyboardDevice,
                                 setOf(InputEventCodes.KEY_LEFTCTRL,
                                       InputEventCodes.KEY_LEFTALT,
                                       InputEventCodes.KEY_F8)) { tty.activate(8) }.enable()

        keyBindingFactory.create(keyboardDevice,
                                 setOf(InputEventCodes.KEY_LEFTCTRL,
                                       InputEventCodes.KEY_LEFTALT,
                                       InputEventCodes.KEY_F9)) { tty.activate(9) }.enable()
        keyBindingFactory.create(keyboardDevice,
                                 setOf(InputEventCodes.KEY_LEFTCTRL,
                                       InputEventCodes.KEY_LEFTALT,
                                       InputEventCodes.KEY_F10)) { tty.activate(10) }.enable()
        keyBindingFactory.create(keyboardDevice,
                                 setOf(InputEventCodes.KEY_LEFTCTRL,
                                       InputEventCodes.KEY_LEFTALT,
                                       InputEventCodes.KEY_F11)) { tty.activate(11) }.enable()
        keyBindingFactory.create(keyboardDevice,
                                 setOf(InputEventCodes.KEY_LEFTCTRL,
                                       InputEventCodes.KEY_LEFTALT,
                                       InputEventCodes.KEY_F12)) { tty.activate(12) }.enable()
    }

    companion object {

        private val LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME)

        @JvmStatic fun main_from_native() {
            try {
                //this main is not launched directly, but instead from native code that invokes the jvm after
                //it has properly configured user rights, OS signals used for tty switching etc.

                configureLogger()
                LOGGER.info("Starting Westford")

                Launcher().launch(DaggerIndirectDrmEglCompositor.create())
            }
            catch (t: Throwable) {
                LOGGER.throwing(Launcher::class.java.name,
                                "main_from_native",
                                t)
                t.printStackTrace()
            }
        }

        @Throws(IOException::class) private fun configureLogger() {
            val fileHandler = FileHandler("westford.log")
            fileHandler.formatter = SimpleFormatter()
            LOGGER.addHandler(fileHandler)

            Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
                LOGGER.severe("Got uncaught exception " + throwable.message)
                throwable.printStackTrace()
            }
        }
    }
}