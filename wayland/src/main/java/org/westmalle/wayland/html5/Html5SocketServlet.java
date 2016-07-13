package org.westmalle.wayland.html5;


import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

@AutoFactory
public class Html5SocketServlet extends WebSocketServlet implements WebSocketCreator {

    private final Html5SocketFactory html5SocketFactory;
    private final Html5Connector     html5Connector;

    Html5SocketServlet(@Provided final Html5SocketFactory html5SocketFactory,
                       final Html5Connector html5Connector) {
        this.html5SocketFactory = html5SocketFactory;
        this.html5Connector = html5Connector;
    }

    @Override
    public void configure(final WebSocketServletFactory factory) {
        factory.setCreator(this);
    }

    @Override
    public Object createWebSocket(final ServletUpgradeRequest req,
                                  final ServletUpgradeResponse resp) {
        return this.html5SocketFactory.create(this.html5Connector);
    }
}
