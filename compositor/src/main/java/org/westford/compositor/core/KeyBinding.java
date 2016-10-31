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
package org.westford.compositor.core;

import com.google.auto.factory.AutoFactory;
import org.freedesktop.wayland.shared.WlKeyboardKeyState;
import org.westford.Slot;
import org.westford.compositor.core.events.Key;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.Set;

@AutoFactory(allowSubclasses = true,
             className = "KeyBindingFactory")
public class KeyBinding {

    @Nonnull
    private final KeyboardDevice keyboardDevice;
    private final Set<Integer>   keys;
    private final Runnable       binding;
    private       Optional<Integer> triggerKey = Optional.empty();
    private final Slot<Key>         handleKey  = this::handleKey;

    KeyBinding(@Nonnull final KeyboardDevice keyboardDevice,
               final Set<Integer> keys,
               final Runnable binding) {
        this.keyboardDevice = keyboardDevice;
        this.keys = keys;
        this.binding = binding;
    }

    //TODO unit test enable/disable & consummation of key events

    public void enable() {
        this.keyboardDevice.getKeySignal()
                           .connect(this.handleKey);
    }

    public void disable() {
        this.keyboardDevice.getKeySignal()
                           .disconnect(this.handleKey);
    }

    private void handleKey(final Key event) {
        final WlKeyboardKeyState keyState = event.getKeyState();
        if (keyState == WlKeyboardKeyState.RELEASED) {
            //the trigger key is released, the hide it from the client.
            this.triggerKey.ifPresent(key -> {
                if (key == event.getKey()) {
                    this.keyboardDevice.consumeNextKeyEvent();
                    this.triggerKey = Optional.empty();
                }
            });
        }
        else {
            //make sure pressed keys match without any additional keys being pressed.
            if (this.keyboardDevice.getPressedKeys()
                                   .size() == this.keys.size() &&
                this.keyboardDevice.getPressedKeys()
                                   .containsAll(this.keys)) {
                //Store the latest key that triggered the binding. This is needed because we must suppress the release of this key as well
                this.triggerKey = Optional.of(event.getKey());
                //this will consume the press of the latest key that fulfills the required keys needed for the binding.
                this.keyboardDevice.consumeNextKeyEvent();
                this.binding.run();
            }
        }
    }
}
