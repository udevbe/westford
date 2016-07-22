package org.westmalle.wayland.html5;


import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.westmalle.wayland.core.KeyboardDeviceFactory;
import org.westmalle.wayland.input.LibinputXkbFactory;
import org.westmalle.wayland.protocol.WlKeyboardFactory;
import org.westmalle.wayland.protocol.WlSeatFactory;

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
