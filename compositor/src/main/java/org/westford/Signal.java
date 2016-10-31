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
package org.westford;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

public class Signal<U, T extends Slot<U>> {

    private final Set<T> slots = new HashSet<>();

    public void connect(@Nonnull final T slot) {
        this.slots.add(slot);
    }

    public void disconnect(@Nonnull final T slot) {
        this.slots.remove(slot);
    }

    public void emit(@Nonnull final U event) {
        new HashSet<>(this.slots).forEach(slot -> slot.handle(event));
    }

    public boolean isConnected(@Nonnull final T slot) {
        return this.slots.contains(slot);
    }
}
