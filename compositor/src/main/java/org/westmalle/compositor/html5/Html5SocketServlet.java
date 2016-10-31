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
import com.google.auto.factory.Provided;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.westmalle.compositor.html5.Html5SocketFactory;

import javax.annotation.Nonnull;

@AutoFactory
public class Html5SocketServlet extends WebSocketServlet implements WebSocketCreator {

    private final Html5SocketFactory html5SocketFactory;

    @Nonnull
    private final Html5RenderOutput html5RenderOutput;

    Html5SocketServlet(@Provided final Html5SocketFactory html5SocketFactory,
                       @Nonnull final Html5RenderOutput html5RenderOutput) {
        this.html5SocketFactory = html5SocketFactory;
        this.html5RenderOutput = html5RenderOutput;
    }

    @Override
    public void configure(final WebSocketServletFactory factory) {
        factory.setCreator(this);
    }

    @Override
    public Object createWebSocket(final ServletUpgradeRequest req,
                                  final ServletUpgradeResponse resp) {
        return this.html5SocketFactory.create(this.html5RenderOutput);
    }
}
