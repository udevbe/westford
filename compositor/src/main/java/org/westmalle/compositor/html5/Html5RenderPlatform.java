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
package org.westmalle.compositor.html5;

import com.google.auto.factory.AutoFactory;
import org.eclipse.jetty.server.Server;
import org.westmalle.Signal;
import org.westmalle.Slot;
import org.westmalle.compositor.core.RenderPlatform;
import org.westmalle.compositor.core.events.RenderOutputDestroyed;
import org.westmalle.compositor.core.events.RenderOutputNew;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.List;

@AutoFactory(allowSubclasses = true,
             className = "PrivateHtml5PlatformFactory")
public class Html5RenderPlatform implements RenderPlatform {

    private final Server                  server;
    private final List<Html5RenderOutput> html5RenderOutputs;
    private final Signal<RenderOutputNew, Slot<RenderOutputNew>>             renderOutputNewSignal       = new Signal<>();
    private final Signal<RenderOutputDestroyed, Slot<RenderOutputDestroyed>> renderOutputDestroyedSignal = new Signal<>();

    @Inject
    Html5RenderPlatform(final Server server,
                        final List<Html5RenderOutput> html5RenderOutputs) {
        this.server = server;
        this.html5RenderOutputs = html5RenderOutputs;
    }

    public Server getServer() {
        return this.server;
    }

    @Nonnull
    @Override
    public List<Html5RenderOutput> getRenderOutputs() {
        return this.html5RenderOutputs;
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
