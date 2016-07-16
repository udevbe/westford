package org.westmalle.wayland.html5;


import com.google.auto.factory.AutoFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.westmalle.wayland.core.Connector;
import org.westmalle.wayland.core.Renderer;
import org.westmalle.wayland.protocol.WlOutput;

import javax.annotation.Nonnull;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.CRC32;
import java.util.zip.ZipOutputStream;

@AutoFactory(allowSubclasses = true,
             className = "Html5ConnectorFactory")
public class Html5Connector implements Connector {

    private final Connector connector;

    private final Set<Html5Socket> html5Sockets = new HashSet<>();

    Html5Connector(final Connector connector) {
        this.connector = connector;
    }

    @Nonnull
    @Override
    public WlOutput getWlOutput() {
        return this.connector.getWlOutput();
    }

    public Set<Html5Socket> getHtml5Sockets() {
        return this.html5Sockets;
    }

    //TODO add methods to send screen updates to all connected sockets
    public void commitFrame(final byte[] raw,
                            final int pitch,
                            final int height) {
        getHtml5Sockets()
                .forEach(html5Socket ->
                                 html5Socket.getSession()
                                            .ifPresent(session -> {
                                                try {
                                                    session.getRemote()
                                                           .sendBytes(ByteBuffer.wrap(toPng(raw,
                                                                                            pitch,
                                                                                            height)));
                                                }
                                                catch (final IOException e) {
                                                    //TODO log
                                                    e.printStackTrace();
                                                }
                                            }));
    }

    private byte[] toPng(final byte[] raw,
                         final int pitch,
                         final int height) throws IOException {

        //TODO use libpng

        return null;
    }

    //TODO handle socket input events

    public void onWebSocketBinary(final Html5Socket html5Socket,
                                  final byte[] payload,
                                  final int offset,
                                  final int len) {
        //no-op
    }

    public void onWebSocketText(final Html5Socket html5Socket,
                                final String message) {

        switch (message) {
            case "req-output-info": {
                handleOutputInfoRequest(html5Socket);
                break;
            }
            case "ack-output-info": {
                //enable sending frames
                this.html5Sockets.add(html5Socket);
                break;
            }
            default: {
                handleInput(message);
            }
        }
    }

    private void handleInput(final String message) {
        switch (message.substring(0,
                                  4)) {
            case "p:d:": {

            }
            case "p:u:": {

            }
            case "p:m:": {

            }
            case "k:d:": {

            }
            case "k:u:": {

            }
        }
    }

    private void handleOutputInfoRequest(final Html5Socket html5Socket) {
        html5Socket.getSession()
                   .ifPresent(session -> {
                       try {
                           //TODO send output info
                           session.getRemote()
                                  .sendString("output info here");
                       }
                       catch (final IOException e) {
                           //TODO log
                           e.printStackTrace();
                       }
                   });
    }

    public void onWebSocketClose(final Html5Socket html5Socket,
                                 final int statusCode,
                                 final String reason) {
        this.html5Sockets.remove(html5Socket);
    }

    public void onWebSocketConnect(final Html5Socket html5Socket,
                                   final Session session) {
        //no op
    }

    public void onWebSocketError(final Html5Socket html5Socket,
                                 final Throwable cause) {
        //TODO log
    }

    @Override
    public void accept(@Nonnull final Renderer renderer) {
        renderer.visit(this.connector);
    }
}
