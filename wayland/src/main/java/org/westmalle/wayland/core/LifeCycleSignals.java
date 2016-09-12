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
package org.westmalle.wayland.core;

import org.westmalle.Signal;
import org.westmalle.Slot;

public class LifeCycleSignals {

    private final Signal<Object, Slot<Object>> activateSignal   = new Signal<>();
    private final Signal<Object, Slot<Object>> deactivateSignal = new Signal<>();
    private final Signal<Object, Slot<Object>> startSignal      = new Signal<>();
    private final Signal<Object, Slot<Object>> stopSignal       = new Signal<>();

    LifeCycleSignals() {
    }

    public Signal<Object, Slot<Object>> getActivateSignal() {
        return this.activateSignal;
    }

    public Signal<Object, Slot<Object>> getDeactivateSignal() {
        return this.deactivateSignal;
    }

    public Signal<Object, Slot<Object>> getStartSignal() {
        return this.startSignal;
    }

    public Signal<Object, Slot<Object>> getStopSignal() {
        return this.stopSignal;
    }
}