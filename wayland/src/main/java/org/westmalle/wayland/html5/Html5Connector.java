package org.westmalle.wayland.html5;


import com.google.auto.factory.AutoFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.westmalle.wayland.core.Connector;
import org.westmalle.wayland.core.Renderer;
import org.westmalle.wayland.protocol.WlOutput;

import javax.annotation.Nonnull;

@AutoFactory(allowSubclasses = true,
             className = "Html5ConnectorFactory")
public class Html5Connector implements Connector {

    private final Connector connector;

    Html5Connector(final Connector connector) {
        this.connector = connector;
    }

    @Nonnull
    @Override
    public WlOutput getWlOutput() {
        return this.connector.getWlOutput();
    }

    //TODO add methods to send screen updates to all connected sockets

    //TODO handle socket input events

    public void onWebSocketBinary(final Html5Socket html5Socket,
                                  final byte[] payload,
                                  final int offset,
                                  final int len) {

    }

    public void onWebSocketText(final Html5Socket html5Socket,
                                final String message) {

    }

    public void onWebSocketClose(final Html5Socket html5Socket,
                                 final int statusCode,
                                 final String reason) {

    }

    public void onWebSocketConnect(final Html5Socket html5Socket,
                                   final Session session) {

    }

    public void onWebSocketError(final Html5Socket html5Socket,
                                 final Throwable cause) {

    }

    @Override
    public void accept(@Nonnull final Renderer renderer) {
        renderer.visit(this.connector);
    }
}
