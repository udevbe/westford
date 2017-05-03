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
package org.westford.compositor.x11

import org.freedesktop.jaccall.Pointer
import org.freedesktop.wayland.server.Display
import org.freedesktop.wayland.server.jaccall.WaylandServerCore
import org.westford.compositor.x11.config.X11OutputConfig
import org.westford.compositor.x11.config.X11PlatformConfig
import org.westford.nativ.libX11.LibX11
import org.westford.nativ.libX11xcb.LibX11xcb
import org.westford.nativ.libX11xcb.LibX11xcb.Companion.XCBOwnsEventQueue
import org.westford.nativ.libxcb.Libxcb
import org.westford.nativ.libxcb.Libxcb.Companion.XCB_ATOM_ATOM
import org.westford.nativ.libxcb.Libxcb.Companion.XCB_COPY_FROM_PARENT
import org.westford.nativ.libxcb.Libxcb.Companion.XCB_CW_EVENT_MASK
import org.westford.nativ.libxcb.Libxcb.Companion.XCB_EVENT_MASK_BUTTON_PRESS
import org.westford.nativ.libxcb.Libxcb.Companion.XCB_EVENT_MASK_BUTTON_RELEASE
import org.westford.nativ.libxcb.Libxcb.Companion.XCB_EVENT_MASK_ENTER_WINDOW
import org.westford.nativ.libxcb.Libxcb.Companion.XCB_EVENT_MASK_FOCUS_CHANGE
import org.westford.nativ.libxcb.Libxcb.Companion.XCB_EVENT_MASK_KEYMAP_STATE
import org.westford.nativ.libxcb.Libxcb.Companion.XCB_EVENT_MASK_KEY_PRESS
import org.westford.nativ.libxcb.Libxcb.Companion.XCB_EVENT_MASK_KEY_RELEASE
import org.westford.nativ.libxcb.Libxcb.Companion.XCB_EVENT_MASK_LEAVE_WINDOW
import org.westford.nativ.libxcb.Libxcb.Companion.XCB_EVENT_MASK_POINTER_MOTION
import org.westford.nativ.libxcb.Libxcb.Companion.XCB_PROP_MODE_REPLACE
import org.westford.nativ.libxcb.Libxcb.Companion.XCB_WINDOW_CLASS_INPUT_OUTPUT
import org.westford.nativ.libxcb.xcb_intern_atom_reply_t
import org.westford.nativ.libxcb.xcb_screen_iterator_t
import org.westford.nativ.libxcb.xcb_screen_t
import java.lang.String.format
import java.util.*
import java.util.logging.Logger
import javax.inject.Inject

class X11PlatformFactory @Inject internal constructor(private val display: Display,
                                                      private val libX11: LibX11,
                                                      private val libxcb: Libxcb,
                                                      private val libX11xcb: LibX11xcb,
                                                      private val privateX11PlatformFactory: PrivateX11PlatformFactory,
                                                      private val x11OutputFactory: X11OutputFactory,
                                                      private val x11EventBusFactory: X11EventBusFactory,
                                                      private val x11PlatformConfig: X11PlatformConfig) {

    fun create(): X11Platform {
        val xDisplayName = this.x11PlatformConfig.display

        LOGGER.info(format("Creating X11 platform:\n" + "\tDisplay: %s",
                           xDisplayName))

        val xDisplay = this.libX11.XOpenDisplay(Pointer.nref(xDisplayName).address)
        if (xDisplay == 0L) {
            throw RuntimeException("XOpenDisplay() failed: " + xDisplayName)
        }

        val xcbConnection = this.libX11xcb.XGetXCBConnection(xDisplay)
        this.libX11xcb.XSetEventQueueOwner(xDisplay,
                                           XCBOwnsEventQueue)
        if (this.libxcb.xcb_connection_has_error(xcbConnection) != 0) {
            throw RuntimeException("error occurred while connecting to X server")
        }

        val x11Platform = createX11Platform(xDisplay,
                                            xcbConnection)

        this.libxcb.xcb_flush(xcbConnection)
        return x11Platform
    }

    private fun createX11Platform(xDisplay: Long,
                                  xcbConnection: Long): X11Platform {
        val x11EventBus = this.x11EventBusFactory.create(xcbConnection)
        val x11Atoms = internX11Atoms(xcbConnection)

        val x11OutputConfigs = this.x11PlatformConfig.x11RenderOutputConfigs
        val x11Outputs = LinkedList<X11Output>()

        val x11Platform = this.privateX11PlatformFactory.create(x11Outputs,
                                                                x11EventBus,
                                                                xcbConnection,
                                                                xDisplay,
                                                                x11Atoms)

        var x = 0
        val y = 0
        for (x11OutputConfig in x11OutputConfigs) {
            addX11RenderOutput(x11Outputs,
                               xcbConnection,
                               x11Atoms,
                               x11OutputConfig,
                               x,
                               y)
            //TODO Add a layout hint. For now, layout from left to right.
            x += x11OutputConfig.width
        }


        this.display.eventLoop.addFileDescriptor(this.libxcb.xcb_get_file_descriptor(xcbConnection),
                                                 WaylandServerCore.WL_EVENT_READABLE,
                                                 x11EventBus).check()

        return x11Platform
    }

    private fun internX11Atoms(connection: Long): Map<String, Int> {
        val atomNames = arrayOf("WM_PROTOCOLS",
                                "WM_NORMAL_HINTS",
                                "WM_SIZE_HINTS",
                                "WM_DELETE_WINDOW",
                                "WM_CLASS",
                                "_NET_WM_NAME",
                                "_NET_WM_ICON",
                                "_NET_WM_STATE",
                                "_NET_WM_STATE_FULLSCREEN",
                                "_NET_SUPPORTING_WM_CHECK",
                                "_NET_SUPPORTED",
                                "STRING",
                                "UTF8_STRING",
                                "CARDINAL",
                                "_XKB_RULES_NAMES")

        val cookies = IntArray(atomNames.size)
        for (i in atomNames.indices) {
            cookies[i] = this.libxcb.xcb_intern_atom(connection,
                                                     0.toByte(),
                                                     atomNames[i].length.toShort(),
                                                     Pointer.nref(atomNames[i]).address)
        }

        val x11Atoms = HashMap<String, Int>(atomNames.size)
        for (i in atomNames.indices) {
            Pointer.wrap<xcb_intern_atom_reply_t>(xcb_intern_atom_reply_t::class.java,
                                                  this.libxcb.xcb_intern_atom_reply(connection,
                                                                                    cookies[i],
                                                                                    0L)).use { reply ->
                x11Atoms.put(atomNames[i],
                             reply.get().atom)
            }
        }
        return x11Atoms
    }

    private fun addX11RenderOutput(x11Outputs: MutableList<X11Output>,
                                   xcbConnection: Long,
                                   x11Atoms: Map<String, Int>,
                                   x11OutputConfig: X11OutputConfig,
                                   x: Int,
                                   y: Int) {

        val width = x11OutputConfig.width
        val height = x11OutputConfig.height
        val outputName = x11OutputConfig.name

        if (width < 0 || height < 0) {
            throw IllegalArgumentException("Got negative width or height")
        }

        val setup = this.libxcb.xcb_get_setup(xcbConnection)
        val screent_iter = Pointer.wrap<xcb_screen_iterator_t>(xcb_screen_iterator_t::class.java,
                                                               this.libxcb.xcb_setup_roots_iterator(setup))
        val screen: xcb_screen_t = screent_iter.get().data.get()
        screent_iter.close()

        val window = this.libxcb.xcb_generate_id(xcbConnection)
        if (window <= 0) {
            throw RuntimeException("failed to generate X window id")
        }

        val values = Pointer.nref(XCB_EVENT_MASK_KEY_PRESS or XCB_EVENT_MASK_KEY_RELEASE or XCB_EVENT_MASK_BUTTON_PRESS or XCB_EVENT_MASK_BUTTON_RELEASE or XCB_EVENT_MASK_ENTER_WINDOW or XCB_EVENT_MASK_LEAVE_WINDOW or XCB_EVENT_MASK_POINTER_MOTION or XCB_EVENT_MASK_KEYMAP_STATE or XCB_EVENT_MASK_FOCUS_CHANGE)
        this.libxcb.xcb_create_window(xcbConnection,
                                      XCB_COPY_FROM_PARENT.toByte(),
                                      window,
                                      screen.root,
                                      x.toShort(),
                                      y.toShort(),
                                      width.toShort(),
                                      height.toShort(),
                                      0.toShort(),
                                      XCB_WINDOW_CLASS_INPUT_OUTPUT.toShort(),
                                      screen.root_visual,
                                      XCB_CW_EVENT_MASK,
                                      values.address)

        setWmProtocol(xcbConnection,
                      window,
                      x11Atoms["WM_PROTOCOLS"]!!,
                      x11Atoms["WM_DELETE_WINDOW"]!!)
        setName(x11OutputConfig,
                xcbConnection,
                window,
                x11Atoms)

        this.libxcb.xcb_map_window(xcbConnection,
                                   window)

        val x11Output = this.x11OutputFactory.create(window,
                                                     x,
                                                     y,
                                                     width,
                                                     height,
                                                     outputName,
                                                     screen)

        x11Outputs.add(x11Output)
    }

    private fun setWmProtocol(connection: Long,
                              window: Int,
                              wmProtocols: Int,
                              wmDeleteWindow: Int) {
        this.libxcb.xcb_change_property(connection,
                                        XCB_PROP_MODE_REPLACE.toByte(),
                                        window,
                                        wmProtocols,
                                        XCB_ATOM_ATOM,
                                        32.toByte(),
                                        1,
                                        Pointer.nref(wmDeleteWindow).address)
    }

    private fun setName(x11OutputConfig: X11OutputConfig,
                        connection: Long,
                        window: Int,
                        x11Atoms: Map<String, Int>) {
        val name = x11OutputConfig.name
        val nameLength = name.length
        val nameNative = Pointer.nref(name).address

        this.libxcb.xcb_change_property(connection,
                                        XCB_PROP_MODE_REPLACE.toByte(),
                                        window,
                                        x11Atoms["_NET_WM_NAME"]!!,
                                        x11Atoms["UTF8_STRING"]!!,
                                        8.toByte(),
                                        nameLength,
                                        nameNative)
        this.libxcb.xcb_change_property(connection,
                                        XCB_PROP_MODE_REPLACE.toByte(),
                                        window,
                                        x11Atoms["WM_CLASS"]!!,
                                        x11Atoms["STRING"]!!,
                                        8.toByte(),
                                        nameLength,
                                        nameNative)
    }

    companion object {

        private val LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME)
    }
}
