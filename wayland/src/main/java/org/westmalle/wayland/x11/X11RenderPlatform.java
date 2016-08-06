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
package org.westmalle.wayland.x11;

import com.google.auto.factory.AutoFactory;
import org.westmalle.wayland.core.RenderPlatform;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

@AutoFactory(className = "PrivateX11PlatformFactory",
             allowSubclasses = true)
public class X11RenderPlatform implements RenderPlatform {

    @Nonnull
    private final List<X11RenderOutput> x11RenderOutputs;
    @Nonnull
    private final X11EventBus           x11EventBus;
    private final long                  xcbConnection;
    private final long                  xDisplay;
    @Nonnull
    private final Map<String, Integer>  atoms;

    X11RenderPlatform(@Nonnull final List<X11RenderOutput> x11RenderOutputs,
                      @Nonnull final X11EventBus x11EventBus,
                      final long xcbConnection,
                      final long xDisplay,
                      @Nonnull final Map<String, Integer> x11Atoms) {
        this.x11RenderOutputs = x11RenderOutputs;
        this.x11EventBus = x11EventBus;
        this.xcbConnection = xcbConnection;
        this.xDisplay = xDisplay;
        this.atoms = x11Atoms;
    }

    public long getxDisplay() {
        return this.xDisplay;
    }

    public long getXcbConnection() {
        return this.xcbConnection;
    }

    @Nonnull
    public X11EventBus getX11EventBus() {
        return this.x11EventBus;
    }

    @Nonnull
    public Map<String, Integer> getX11Atoms() {
        return this.atoms;
    }

    @Nonnull
    @Override
    public List<X11RenderOutput> getRenderOutputs() {
        return this.x11RenderOutputs;
    }
}
