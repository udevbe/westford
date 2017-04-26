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
import org.freedesktop.wayland.shared.WlKeyboardKeyState
import org.westford.Slot
import org.westford.compositor.core.events.Key
import java.util.Optional

@AutoFactory(allowSubclasses = true, className = "KeyBindingFactory")
class KeyBinding internal constructor(private val keyboardDevice: KeyboardDevice,
                                      private val keys: Set<Int>,
                                      private val binding: Runnable) {
    private var triggerKey = Optional.empty<Int>()
    private val handleKey = Slot<Key> { this.handleKey(it) }

    //TODO unit test enable/disable & consummation of key events

    fun enable() {
        this.keyboardDevice.keySignal
                .connect(this.handleKey)
    }

    fun disable() {
        this.keyboardDevice.keySignal
                .disconnect(this.handleKey)
    }

    private fun handleKey(event: Key) {
        val keyState = event.keyState
        if (keyState == WlKeyboardKeyState.RELEASED) {
            //the trigger key is released, the hide it from the client.
            this.triggerKey.ifPresent { key ->
                if (key === event.key) {
                    this.keyboardDevice.consumeNextKeyEvent()
                    this.triggerKey = Optional.empty<Int>()
                }
            }
        } else {
            //make sure pressed keys match without any additional keys being pressed.
            if (this.keyboardDevice.pressedKeys
                    .size == this.keys.size && this.keyboardDevice.pressedKeys
                    .containsAll(this.keys)) {
                //Store the latest key that triggered the binding. This is needed because we must suppress the release of this key as well
                this.triggerKey = Optional.of(event.key)
                //this will consume the press of the latest key that fulfills the required keys needed for the binding.
                this.keyboardDevice.consumeNextKeyEvent()
                this.binding.run()
            }
        }
    }
}
