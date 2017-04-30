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
package org.westford.compositor.core

import com.google.auto.factory.AutoFactory
import com.google.auto.factory.Provided
import org.freedesktop.jaccall.Pointer
import org.freedesktop.wayland.server.Client
import org.freedesktop.wayland.server.Display
import org.freedesktop.wayland.server.WlKeyboardResource
import org.freedesktop.wayland.server.WlSurfaceResource
import org.freedesktop.wayland.shared.WlKeyboardKeyState
import org.freedesktop.wayland.shared.WlKeyboardKeymapFormat
import org.westford.Signal
import org.westford.compositor.core.events.Key
import org.westford.compositor.core.events.KeyboardFocus
import org.westford.compositor.core.events.KeyboardFocusGained
import org.westford.compositor.core.events.KeyboardFocusLost
import org.westford.compositor.protocol.WlSurface
import org.westford.nativ.NativeFileFactory
import org.westford.nativ.glibc.Libc
import org.westford.nativ.libxkbcommon.Libxkbcommon
import org.westford.nativ.libxkbcommon.Libxkbcommon.Companion.XKB_KEY_DOWN
import org.westford.nativ.libxkbcommon.Libxkbcommon.Companion.XKB_KEY_UP
import org.westford.nativ.libxkbcommon.Libxkbcommon.Companion.XKB_STATE_LAYOUT_EFFECTIVE
import org.westford.nativ.libxkbcommon.Libxkbcommon.Companion.XKB_STATE_MODS_DEPRESSED
import org.westford.nativ.libxkbcommon.Libxkbcommon.Companion.XKB_STATE_MODS_LATCHED
import org.westford.nativ.libxkbcommon.Libxkbcommon.Companion.XKB_STATE_MODS_LOCKED
import java.nio.ByteBuffer
import java.util.*
import javax.annotation.Nonnegative

@AutoFactory(className = "KeyboardDeviceFactory",
             allowSubclasses = true) class KeyboardDevice(@param:Provided private val display: Display,
                                                          @param:Provided private val nativeFileFactory: NativeFileFactory,
                                                          @param:Provided private val libc: Libc,
                                                          @param:Provided private val libxkbcommon: Libxkbcommon,
                                                          var xkb: Xkb) {

    val keySignal = Signal<Key>()
    val keyboardFocusSignal = Signal<KeyboardFocus>()
    val pressedKeys = mutableSetOf<Int>()
    var focus: WlSurfaceResource? = null; private set
    var keyboardSerial: Int = 0; private set

    private var focusDestroyListener: (() -> Unit)? = null
    private var keymapFd = -1
    @Nonnegative private var keymapSize = 0
    private var consumeNextKeyEvent: Boolean = false

    /**
     * Find the keyboard focused surface and deliver a key event to the client of the focused surface.

     * @param wlKeyboardResources A set of all keyboard resources that will be used to find the client.
     * *
     * @param key                 the key to deliver
     * *
     * @param wlKeyboardKeyState  the state of the key.
     */
    fun key(wlKeyboardResources: Set<WlKeyboardResource>,
            time: Int,
            key: Int,
            wlKeyboardKeyState: WlKeyboardKeyState) {

        var stateComponentMask = 0
        val xkbState = xkb.state
        val evdevKey = key + 8

        if (wlKeyboardKeyState == WlKeyboardKeyState.PRESSED) {
            if (pressedKeys.add(key)) {
                stateComponentMask = this.libxkbcommon.xkb_state_update_key(xkbState,
                                                                            evdevKey,
                                                                            XKB_KEY_DOWN)
            }
        }
        else {
            if (pressedKeys.remove(key)) {
                stateComponentMask = this.libxkbcommon.xkb_state_update_key(xkbState,
                                                                            evdevKey,
                                                                            XKB_KEY_UP)
            }
        }

        this.keySignal.emit(Key.create(time,
                                       key,
                                       wlKeyboardKeyState))

        if (this.consumeNextKeyEvent) {
            this.consumeNextKeyEvent = false
        }
        else {
            doKey(wlKeyboardResources,
                  time,
                  key,
                  wlKeyboardKeyState)

            handleStateComponentMask(wlKeyboardResources,
                                     stateComponentMask)
        }
    }

    private fun doKey(wlKeyboardResources: Set<WlKeyboardResource>,
                      time: Int,
                      key: Int,
                      wlKeyboardKeyState: WlKeyboardKeyState) {
        focus?.let {
            match(wlKeyboardResources,
                  it).forEach {
                it.key(nextKeyboardSerial(),
                       time,
                       key,
                       wlKeyboardKeyState.value)
            }
        }
    }

    private fun handleStateComponentMask(wlKeyboardResources: Set<WlKeyboardResource>,
                                         stateComponentMask: Int) {
        if (stateComponentMask and (XKB_STATE_MODS_DEPRESSED or XKB_STATE_MODS_LATCHED or XKB_STATE_MODS_LOCKED or XKB_STATE_LAYOUT_EFFECTIVE) != 0) {
            val modsDepressed = this.libxkbcommon.xkb_state_serialize_mods(xkb.state,
                                                                           XKB_STATE_MODS_DEPRESSED)
            val modsLatched = this.libxkbcommon.xkb_state_serialize_mods(xkb.state,
                                                                         XKB_STATE_MODS_LATCHED)
            val modsLocked = this.libxkbcommon.xkb_state_serialize_mods(xkb.state,
                                                                        XKB_STATE_MODS_LOCKED)
            val group = this.libxkbcommon.xkb_state_serialize_layout(xkb.state,
                                                                     XKB_STATE_LAYOUT_EFFECTIVE)
            wlKeyboardResources.forEach { wlKeyboardResource ->
                wlKeyboardResource.modifiers(this.display.nextSerial(),
                                             modsDepressed,
                                             modsLatched,
                                             modsLocked,
                                             group)
            }
        }
    }

    private fun match(wlKeyboardResources: Set<WlKeyboardResource>,
                      wlSurfaceResource: WlSurfaceResource): Set<WlKeyboardResource> {
        //find keyboard resources that match this keyboard device
        val wlSurface = wlSurfaceResource.implementation as WlSurface
        val surface = wlSurface.surface
        val keyboardFocuses = HashSet(surface.keyboardFocuses)
        keyboardFocuses.retainAll(wlKeyboardResources)

        return keyboardFocuses
    }

    fun nextKeyboardSerial(): Int {
        this.keyboardSerial = this.display.nextSerial()
        return this.keyboardSerial
    }

    fun consumeNextKeyEvent() {
        this.consumeNextKeyEvent = true
    }

    fun setFocus(wlKeyboardResources: Set<WlKeyboardResource>,
                 newFocus: WlSurfaceResource?) {
        val oldFocus = focus
        if (oldFocus != newFocus) {
            updateFocus(wlKeyboardResources,
                        oldFocus,
                        newFocus)
        }
    }

    private fun toIntArray(set: Set<Int>): IntArray {
        val ret = IntArray(set.size)
        var i = 0
        for (e in set) {
            ret[i++] = e
        }
        return ret
    }

    private fun updateFocus(wlKeyboardResources: Set<WlKeyboardResource>,
                            oldFocus: WlSurfaceResource?,
                            newFocus: WlSurfaceResource?) {
        this.focus = newFocus
        keyboardFocusSignal.emit(KeyboardFocus.create(newFocus))

        oldFocus?.let {
            it.unregister(this.focusDestroyListener)
            this.focusDestroyListener = null

            val wlSurface = it.implementation as WlSurface
            val surface = wlSurface.surface

            val clientKeyboardResources = filter(wlKeyboardResources,
                                                 it.client)
            surface.keyboardFocuses.minus(clientKeyboardResources)
            surface.keyboardFocusLostSignal.emit(KeyboardFocusLost.create(clientKeyboardResources))

            clientKeyboardResources.forEach { oldFocusKeyboardResource ->
                oldFocusKeyboardResource.leave(nextKeyboardSerial(),
                                               it)
            }
        }

        newFocus?.let {
            this.focusDestroyListener = {
                updateFocus(wlKeyboardResources,
                            newFocus,
                            null)
            }
            it.register(this.focusDestroyListener)

            val wlSurface = it.implementation as WlSurface
            val surface = wlSurface.surface

            val clientKeyboardResources = filter(wlKeyboardResources,
                                                 it.client)
            surface.keyboardFocuses += clientKeyboardResources
            surface.keyboardFocusGainedSignal.emit(KeyboardFocusGained.create(clientKeyboardResources))

            match(wlKeyboardResources,
                  it).forEach { newFocusKeyboardResource ->
                val keys = ByteBuffer.allocateDirect(Integer.BYTES * this.pressedKeys.size)
                keys.asIntBuffer().put(toIntArray(pressedKeys))
                newFocusKeyboardResource.enter(nextKeyboardSerial(),
                                               it,
                                               keys)
            }
        }
    }

    /**
     * filter out keyboard resources that do not belong to the given client.
     */
    private fun filter(wlKeyboardResources: Set<WlKeyboardResource>,
                       client: Client): Set<WlKeyboardResource> {
        return wlKeyboardResources.filter {
            it.client == client
        }.toSet()
    }

    fun emitKeymap(wlKeyboardResources: Set<WlKeyboardResource>) {
        if (this.keymapFd >= 0) {
            wlKeyboardResources.forEach {
                it.keymap(WlKeyboardKeymapFormat.XKB_V1.value,
                          this.keymapFd,
                          this.keymapSize)
            }
        }
    }

    fun updateKeymap() {
        val nativeKeyMapping = xkb.keymapString

        val size = nativeKeyMapping.length
        val fd = this.nativeFileFactory.createAnonymousFile(size)
        val keymapArea = this.libc.mmap(0L,
                                        size,
                                        Libc.PROT_READ or Libc.PROT_WRITE,
                                        Libc.MAP_SHARED,
                                        fd,
                                        0)
        if (keymapArea == Libc.MAP_FAILED) {
            this.libc.close(fd)
            throw Error("MAP_FAILED: " + this.libc.errno)
        }

        this.libc.strcpy(keymapArea,
                         Pointer.nref(nativeKeyMapping).address)

        if (this.keymapFd >= 0) {
            this.libc.close(this.keymapFd)
        }
        this.keymapFd = fd
        this.keymapSize = size
    }
}
