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
package org.westford.launch.direct;

import org.westford.Signal;
import org.westford.Slot;
import org.westford.compositor.core.events.Activate;
import org.westford.compositor.core.events.Deactivate;
import org.westford.compositor.core.events.Start;
import org.westford.compositor.core.events.Stop;
import org.westford.launch.LifeCycleSignals;

public class DirectLifeCycleSignals implements LifeCycleSignals {

    private final Signal<Activate, Slot<Activate>>     activateSignal   = new Signal<>();
    private final Signal<Deactivate, Slot<Deactivate>> deactivateSignal = new Signal<>();
    private final Signal<Start, Slot<Start>>           startSignal      = new Signal<>();
    private final Signal<Stop, Slot<Stop>>             stopSignal       = new Signal<>();

    DirectLifeCycleSignals() {
    }

    @Override
    public Signal<Activate, Slot<Activate>> getActivateSignal() {
        return this.activateSignal;
    }

    @Override
    public Signal<Deactivate, Slot<Deactivate>> getDeactivateSignal() {
        return this.deactivateSignal;
    }

    @Override
    public Signal<Start, Slot<Start>> getStartSignal() {
        return this.startSignal;
    }

    @Override
    public Signal<Stop, Slot<Stop>> getStopSignal() {
        return this.stopSignal;
    }
}