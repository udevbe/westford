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
import com.google.auto.factory.Provided;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import javax.annotation.Nonnull;

@AutoFactory
public class Html5SocketServlet extends WebSocketServlet implements WebSocketCreator {

    private final Html5SocketFactory html5SocketFactory;

    private final Html5SeatFactory html5SeatFactory;
    @Nonnull
    private final Html5Connector   html5Connector;

    Html5SocketServlet(@Provided final Html5SocketFactory html5SocketFactory,
                       @Provided final Html5SeatFactory html5SeatFactory,
                       @Nonnull final Html5Connector html5Connector) {
        this.html5SocketFactory = html5SocketFactory;
        this.html5SeatFactory = html5SeatFactory;
        this.html5Connector = html5Connector;
    }

    @Override
    public void configure(final WebSocketServletFactory factory) {
        factory.setCreator(this);
    }

    @Override
    public Object createWebSocket(final ServletUpgradeRequest req,
                                  final ServletUpgradeResponse resp) {
        //TODO move seat creation to a separate seat event coming from the client so we can properly configure the seat
        return this.html5SocketFactory.create(this.html5Connector,
                                              this.html5SeatFactory.create());
    }
}
