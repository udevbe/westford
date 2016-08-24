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
import org.westmalle.Signal;
import org.westmalle.Slot;
import org.westmalle.wayland.core.RenderPlatform;
import org.westmalle.wayland.core.events.RenderOutputDestroyed;
import org.westmalle.wayland.core.events.RenderOutputNew;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

@AutoFactory(className = "PrivateX11PlatformFactory",
             allowSubclasses = true)
public class X11Platform implements RenderPlatform {

    @Nonnull
    private final List<X11Output> x11Outputs;
    private final Signal<RenderOutputNew, Slot<RenderOutputNew>>             renderOutputNewSignal       = new Signal<>();
    private final Signal<RenderOutputDestroyed, Slot<RenderOutputDestroyed>> renderOutputDestroyedSignal = new Signal<>();

    @Nonnull
    private final X11EventBus          x11EventBus;
    private final long                 xcbConnection;
    private final long                 xDisplay;
    @Nonnull
    private final Map<String, Integer> atoms;

    X11Platform(@Nonnull final List<X11Output> x11Outputs,
                @Nonnull final X11EventBus x11EventBus,
                final long xcbConnection,
                final long xDisplay,
                @Nonnull final Map<String, Integer> x11Atoms) {
        this.x11Outputs = x11Outputs;
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
    public List<X11Output> getRenderOutputs() {
        return this.x11Outputs;
    }

    @Override
    public Signal<RenderOutputNew, Slot<RenderOutputNew>> getRenderOutputNewSignal() {
        return this.renderOutputNewSignal;
    }

    @Override
    public Signal<RenderOutputDestroyed, Slot<RenderOutputDestroyed>> getRenderOutputDestroyedSignal() {
        return this.renderOutputDestroyedSignal;
    }
}
