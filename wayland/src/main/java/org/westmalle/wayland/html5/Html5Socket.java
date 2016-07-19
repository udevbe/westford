package org.westmalle.wayland.html5;


import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.westmalle.wayland.core.JobExecutor;

import javax.annotation.Nonnull;
import java.util.Optional;

@AutoFactory
public class Html5Socket implements WebSocketListener {

    @Nonnull
    private final JobExecutor    jobExecutor;
    private final Html5Connector html5Connector;

    private Optional<Session> session = Optional.empty();

    Html5Socket(@Provided @Nonnull final JobExecutor jobExecutor,
                final Html5Connector html5Connector) {
        this.jobExecutor = jobExecutor;
        this.html5Connector = html5Connector;
    }

    //we offload all incoming events from the web server thread to our own main thread.
    @Override
    public void onWebSocketBinary(final byte[] payload,
                                  final int offset,
                                  final int len) {
        this.jobExecutor.submit(() -> this.html5Connector.onWebSocketBinary(this,
                                                                            payload,
                                                                            offset,
                                                                            len));
    }

    @Override
    public void onWebSocketText(final String message) {
        this.jobExecutor.submit(() -> this.html5Connector.onWebSocketText(this,
                                                                          message));
    }

    @Override
    public void onWebSocketClose(final int statusCode,
                                 final String reason) {
        this.jobExecutor.submit(() -> {
            this.session = Optional.empty();
            this.html5Connector.onWebSocketClose(this,
                                                 statusCode,
                                                 reason);
        });
    }

    @Override
    public void onWebSocketConnect(final Session session) {
        this.jobExecutor.submit(() -> {
            this.session = Optional.of(session);
            this.html5Connector.onWebSocketConnect(this,
                                                   session);
        });
    }

    @Override
    public void onWebSocketError(final Throwable cause) {
        this.jobExecutor.submit(() -> this.html5Connector.onWebSocketError(this,
                                                                           cause));
    }

    public Optional<Session> getSession() {
        return this.session;
    }
}
