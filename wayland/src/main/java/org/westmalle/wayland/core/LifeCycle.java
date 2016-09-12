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

import org.freedesktop.wayland.server.Display;
import org.westmalle.Signal;
import org.westmalle.Slot;

import javax.annotation.Nonnull;
import javax.inject.Inject;

public class LifeCycle implements AutoCloseable {

    private final Signal<Object, Slot<Object>> activateSignal   = new Signal<>();
    private final Signal<Object, Slot<Object>> deactivateSignal = new Signal<>();
    private final Signal<Object, Slot<Object>> startSignal      = new Signal<>();
    private final Signal<Object, Slot<Object>> stopSignal       = new Signal<>();

    @Nonnull
    private final Display     display;
    @Nonnull
    private final JobExecutor jobExecutor;

    @Inject
    LifeCycle(@Nonnull final Display display,
              @Nonnull final JobExecutor jobExecutor) {
        this.display = display;
        this.jobExecutor = jobExecutor;
    }

    public void start() {
        this.jobExecutor.start();
        this.display.initShm();
        this.display.addSocket("wayland-0");
        this.startSignal.emit("START");
        this.display.run();
    }

    @Override
    public void close() {
        this.stopSignal.emit("CLOSE");
        this.jobExecutor.fireFinishedEvent();
        this.display.terminate();
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