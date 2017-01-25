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
package org.westford.compositor.x11;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import org.westford.compositor.core.OutputGeometry;
import org.westford.compositor.core.Point;
import org.westford.compositor.core.RenderOutput;
import org.westford.compositor.core.Renderer;
import org.westford.compositor.protocol.WlOutput;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.Optional;

@AutoFactory(allowSubclasses = true,
             className = "X11OutputFactory")
public class X11Output implements RenderOutput {

    @Nonnull
    private final Renderer renderer;
    private final LinkedList<Renderer> customRenderers = new LinkedList<>();

    private final int      xWindow;
    @Nonnull
    private final WlOutput wlOutput;

    X11Output(@Nonnull @Provided final Renderer renderer,
              final int xWindow,
              @Nonnull final WlOutput wlOutput) {
        this.renderer = renderer;
        this.xWindow = xWindow;
        this.wlOutput = wlOutput;
    }

    public int getXWindow() {
        return this.xWindow;
    }

    public Point toGlobal(final int x11WindowX,
                          final int x11WindowY) {
        final OutputGeometry geometry = getWlOutput().getOutput()
                                                     .getGeometry();
        final int globalX = geometry.getX() + x11WindowX;
        final int globalY = geometry.getY() + x11WindowY;

        return Point.create(globalX,
                            globalY);
    }

    @Nonnull
    @Override
    public WlOutput getWlOutput() {
        return this.wlOutput;
    }

    @Override
    public void push(@Nonnull final Renderer renderer) {
        this.customRenderers.push(renderer);
    }

    @Override
    public Optional<Renderer> popRenderer() {
        return Optional.ofNullable(this.customRenderers.pollFirst());
    }

    @Override
    public void render() {
        Renderer activeRender = this.customRenderers.getFirst();
        if (activeRender == null) {
            activeRender = this.renderer;
        }
        activeRender.visit(this);
    }
}
