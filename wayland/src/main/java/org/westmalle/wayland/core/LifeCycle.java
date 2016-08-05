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
import org.westmalle.wayland.protocol.WlCompositor;
import org.westmalle.wayland.protocol.WlDataDeviceManager;
import org.westmalle.wayland.protocol.WlShell;
import org.westmalle.wayland.protocol.WlSubcompositor;

import javax.annotation.Nonnull;
import javax.inject.Inject;

public class LifeCycle implements AutoCloseable {

    @Nonnull
    private final Display             display;
    @Nonnull
    private final JobExecutor         jobExecutor;
    @Nonnull
    private final WlCompositor        wlCompositor;
    @Nonnull
    private final WlDataDeviceManager wlDataDeviceManager;
    @Nonnull
    private final WlShell             wlShell;
    @Nonnull
    private final WlSubcompositor     wlSubcompositor;

    @Inject
    LifeCycle(@Nonnull final Display display,
              @Nonnull final JobExecutor jobExecutor,
              @Nonnull final WlCompositor wlCompositor,
              @Nonnull final WlDataDeviceManager wlDataDeviceManager,
              @Nonnull final WlShell wlShell,
              @Nonnull final WlSubcompositor wlSubcompositor) {
        this.display = display;
        this.jobExecutor = jobExecutor;
        this.wlCompositor = wlCompositor;
        this.wlDataDeviceManager = wlDataDeviceManager;
        this.wlShell = wlShell;
        this.wlSubcompositor = wlSubcompositor;
    }

    public void start() {
        this.jobExecutor.start();
        this.display.initShm();
        this.display.addSocket("wayland-0");
        this.display.run();
    }

    @Override
    public void close() {
        this.wlCompositor.destroy();
        this.wlDataDeviceManager.destroy();
        this.wlShell.destroy();
        this.wlSubcompositor.destroy();

        this.jobExecutor.fireFinishedEvent();
        this.display.terminate();
    }
}