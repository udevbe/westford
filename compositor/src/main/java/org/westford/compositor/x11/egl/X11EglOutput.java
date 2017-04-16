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
package org.westford.compositor.x11.egl;


import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import org.freedesktop.wayland.server.Display;
import org.westford.compositor.core.EglOutput;
import org.westford.compositor.core.EglOutputState;
import org.westford.compositor.core.Scene;
import org.westford.compositor.core.Subscene;
import org.westford.compositor.core.SurfaceView;
import org.westford.compositor.gles2.Gles2Painter;
import org.westford.compositor.gles2.Gles2PainterFactory;
import org.westford.compositor.gles2.Gles2Renderer;
import org.westford.compositor.protocol.WlOutput;
import org.westford.compositor.x11.X11Output;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

@AutoFactory(allowSubclasses = true,
             className = "X11EglOutputFactory")
public class X11EglOutput implements EglOutput {

    @Nonnull
    private final Gles2PainterFactory gles2PainterFactory;
    @Nonnull
    private final Scene               scene;
    @Nonnull
    private final X11Output           x11Output;
    @Nonnull
    private final Display             display;
    private final long                eglSurface;
    private final long                eglContext;
    private final long                eglDisplay;

    private boolean renderScheduled = false;

    private Optional<EglOutputState> state = Optional.empty();

    X11EglOutput(@Nonnull @Provided final Display display,
                 @Nonnull @Provided final org.westford.compositor.gles2.Gles2PainterFactory gles2PainterFactory,
                 @Nonnull @Provided final Scene scene,
                 @Nonnull final X11Output x11Output,
                 final long eglSurface,
                 final long eglContext,
                 final long eglDisplay) {
        this.display = display;
        this.gles2PainterFactory = gles2PainterFactory;
        this.scene = scene;
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
    public X11Output getX11Output() {
        return this.x11Output;
    }

    @Override
    public void render(@Nonnull final WlOutput wlOutput) {
        //TODO unit test 2 cases here: schedule idle, no-op when already scheduled
        whenIdleDoRender(wlOutput);
    }

    private void whenIdleDoRender(@Nonnull final WlOutput wlOutput) {
        if (!this.renderScheduled) {
            this.renderScheduled = true;
            this.display.getEventLoop()
                        .addIdle(() -> doRender(wlOutput));
        }
    }

    private void doRender(@Nonnull final WlOutput wlOutput) {
        paint(wlOutput);
        this.display.flushClients();
        this.renderScheduled = false;
    }

    private void paint(@Nonnull final WlOutput wlOutput) {

        final Subscene subscene = this.scene.subsection(wlOutput.getOutput()
                                                                .getRegion());

        try (final Gles2Painter gles2Painter = this.gles2PainterFactory.create(this,
                                                                               wlOutput)) {

            //naive generic single pass, bottom to top overdraw rendering.
            final List<SurfaceView>     lockViews      = subscene.getLockViews();
            final Optional<SurfaceView> fullscreenView = subscene.getFullscreenView();

            //lockscreen(s) hide all other screens.
            if (!lockViews.isEmpty()) {
                lockViews.forEach(gles2Painter::paint);
            }
            else {
                fullscreenView.ifPresent(fullscreenSurfaceView -> {
                    //try painting fullscreen view
                    if (!gles2Painter.paint(fullscreenSurfaceView)) {
                        //fullscreen view not visible, paint the rest of the subscene.
                        subscene.getBackgroundView()
                                .ifPresent(gles2Painter::paint);
                        subscene.getUnderViews()
                                .forEach(gles2Painter::paint);
                        subscene.getApplicationViews()
                                .forEach(gles2Painter::paint);
                        subscene.getOverViews()
                                .forEach(gles2Painter::paint);
                    }
                });
            }

            //TODO try utilizing hw cursor plane
            subscene.geCursorViews()
                    .forEach(gles2Painter::paint);
        }
    }
}
