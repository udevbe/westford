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
package org.westmalle.wayland.x11.egl;


import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.EventLoop;
import org.westmalle.wayland.core.EglOutput;
import org.westmalle.wayland.core.EglOutputState;
import org.westmalle.wayland.core.Renderer;
import org.westmalle.wayland.protocol.WlOutput;
import org.westmalle.wayland.x11.X11Output;

import javax.annotation.Nonnull;
import java.util.Optional;

@AutoFactory(allowSubclasses = true,
             className = "X11EglOutputFactory")
public class X11EglOutput implements EglOutput {

    @Nonnull
    private final Renderer  renderer;
    @Nonnull
    private final X11Output x11Output;
    @Nonnull
    private final Display   display;
    private final long      eglSurface;
    private final long      eglContext;
    private final long      eglDisplay;

    private boolean renderScheduled = false;

    private final EventLoop.IdleHandler doRender = this::doRender;

    private Optional<EglOutputState> state = Optional.empty();

    X11EglOutput(@Nonnull @Provided final Display display,
                 @Nonnull @Provided final Renderer renderer,
                 @Nonnull final X11Output x11Output,
                 final long eglSurface,
                 final long eglContext,
                 final long eglDisplay) {
        this.display = display;
        this.renderer = renderer;
        this.x11Output = x11Output;
        this.eglSurface = eglSurface;
        this.eglContext = eglContext;
        this.eglDisplay = eglDisplay;
    }

    @Override
    public long getEglSurface() {
        return this.eglSurface;
    }

    @Override
    public long getEglContext() {
        return this.eglContext;
    }

    @Override
    public long getEglDisplay() {
        return this.eglDisplay;
    }

    @Nonnull
    @Override
    public Optional<EglOutputState> getState() {
        return this.state;
    }

    @Override
    public void updateState(@Nonnull final EglOutputState eglOutputState) {
        this.state = Optional.of(eglOutputState);
    }

    @Nonnull
    @Override
    public WlOutput getWlOutput() {
        return this.x11Output.getWlOutput();
    }

    @Nonnull
    public X11Output getX11Output() {
        return this.x11Output;
    }

    @Override
    public void render() {
        //TODO unit test 2 cases here: schedule idle, no-op when already scheduled
        whenIdleDoRender();
    }

    private void whenIdleDoRender() {
        if (!this.renderScheduled) {
            this.renderScheduled = true;
            this.display.getEventLoop()
                        .addIdle(this.doRender);
        }
    }

    private void doRender() {
        this.renderer.visit(this);
        this.display.flushClients();
        this.renderScheduled = false;
    }
}
