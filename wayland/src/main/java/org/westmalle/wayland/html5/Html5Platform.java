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
package org.westmalle.wayland.html5;

import com.google.auto.factory.AutoFactory;
import org.eclipse.jetty.server.Server;
import org.westmalle.wayland.core.Platform;
import org.westmalle.wayland.core.Renderer;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

@AutoFactory(allowSubclasses = true,
             className = "PrivateHtml5PlatformFactory")
public class Html5Platform implements Platform {

    private final Server                         server;
    private final List<Optional<Html5Connector>> connectors;

    @Inject
    Html5Platform(final Server server,
                  final List<Optional<Html5Connector>> connectors) {
        this.server = server;
        this.connectors = connectors;
    }

    public Server getServer() {
        return this.server;
    }

    @Nonnull
    @Override
    public List<Optional<Html5Connector>> getConnectors() {
        return this.connectors;
    }
}
