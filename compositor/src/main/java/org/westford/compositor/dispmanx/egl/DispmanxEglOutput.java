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
package org.westford.compositor.dispmanx.egl;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import org.freedesktop.wayland.server.Display;
import org.westford.compositor.core.EglOutput;
import org.westford.compositor.core.EglOutputState;
import org.westford.compositor.core.Renderer;
import org.westford.compositor.dispmanx.DispmanxOutput;
import org.westford.compositor.protocol.WlOutput;
import org.westford.nativ.libbcm_host.EGL_DISPMANX_WINDOW_T;

import javax.annotation.Nonnull;
import java.util.Optional;

@AutoFactory(className = "DispmanxEglOutputFactory",
             allowSubclasses = true)
public class DispmanxEglOutput implements EglOutput {

    @Nonnull
    private final Display               display;
    @Nonnull
    private final Renderer              renderer;
    @Nonnull
    private final DispmanxOutput        dispmanxOutput;
    @Nonnull
    private final EGL_DISPMANX_WINDOW_T eglDispmanxWindow;
    private final long                  eglSurface;
    private final long                  eglContext;
    private final long                  eglDisplay;
    private boolean                  renderScheduled = false;
    private Optional<EglOutputState> state           = Optional.empty();

    DispmanxEglOutput(@Nonnull @Provided final Display display,
                      @Nonnull @Provided final Renderer renderer,
                      @Nonnull final DispmanxOutput dispmanxOutput,
                      @Nonnull final EGL_DISPMANX_WINDOW_T eglDispmanxWindow,
                      final long eglSurface,
                      final long eglContext,
                      final long eglDisplay) {
        this.display = display;
        this.renderer = renderer;
        this.dispmanxOutput = dispmanxOutput;
        this.eglDispmanxWindow = eglDispmanxWindow;
        this.eglSurface = eglSurface;
        this.eglContext = eglContext;
        this.eglDisplay = eglDisplay;
    }

    @Nonnull
    public DispmanxOutput getDispmanxOutput() {
        return this.dispmanxOutput;
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
    public EGL_DISPMANX_WINDOW_T getEglDispmanxWindow() {
        return this.eglDispmanxWindow;
    }

    @Nonnull
    @Override
    public WlOutput getWlOutput() {
        return this.dispmanxOutput.getWlOutput();
    }

    @Override
    public void render() {
        //TODO unit test 2 cases here: schedule idle, no-op when already scheduled
        if (!this.renderScheduled) {
            this.renderScheduled = true;
            whenIdleRender();
        }
    }

    private void whenIdleRender() {
        this.display.getEventLoop()
                    .addIdle(() -> {
                        this.renderer.visit(this);
                        this.display.flushClients();
                        this.renderScheduled = false;
                    });
    }
}
