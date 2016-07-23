package org.westmalle.wayland.html5;


import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import org.eclipse.jetty.websocket.api.BatchMode;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.westmalle.wayland.core.JobExecutor;
import org.westmalle.wayland.protocol.WlSeat;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@AutoFactory
public class Html5Socket implements WebSocketListener {

    @Nonnull
    private final JobExecutor    jobExecutor;
    @Nonnull
    private final Html5Connector html5Connector;
    @Nonnull
    private final Html5Seat      html5Seat;

    private Optional<Session> session = Optional.empty();

    private final ExecutorService socketThread = Executors.newSingleThreadExecutor();

    private final AtomicBoolean renderPending = new AtomicBoolean(true);

    private Optional<ByteBuffer> pendingBuffer = Optional.empty();
    private long pendingBufferAge;

    private Optional<ByteBuffer> renderedBuffer = Optional.empty();
    private long renderedBufferAge;


    Html5Socket(@Provided @Nonnull final JobExecutor jobExecutor,
                @Nonnull final Html5Connector html5Connector,
                @Nonnull final Html5Seat html5Seat) {
        this.jobExecutor = jobExecutor;
        this.html5Connector = html5Connector;
        this.html5Seat = html5Seat;
    }

    public void handlePngBuffer(final long pngBufferAge,
                                final Optional<ByteBuffer> pngBuffer) {
        this.socketThread.submit(() -> this.session.ifPresent(session -> {
            //client is done drawing, query the latest available pang buffer
            this.pendingBufferAge = pngBufferAge;
            this.pendingBuffer = pngBuffer;

            if (this.renderedBufferAge == this.pendingBufferAge) {
                //buffer was not updated, don't send it out.
                this.renderPending.set(false);
                return;
            }

            this.pendingBuffer.ifPresent(buffer -> {
                //buffer is present, schedule client draw event

                //TODO we should send frame where unchanged pixels have an rgba int of 0,
                //this will make the png compression much better. To do this we need to compare the latest
                //renderedBuffer with our pending buffer
                try {
                    //TODO use sendByFuture and use common thread pool to listen for failed futures & set render pending to false.
                    session.getRemote()
                           .sendBytes(buffer);
                }
                catch (final IOException e) {
                    this.renderPending.set(false);
                    e.printStackTrace();
                }
            });
        }));
    }

    //we offload all incoming events from the web server thread to our own main thread.
    @Override
    public void onWebSocketBinary(final byte[] payload,
                                  final int offset,
                                  final int len) {
        //no op
    }

    @Override
    public void onWebSocketText(final String message) {
        this.socketThread.execute(() -> handleWebSocketText(message));
    }

    private void handleWebSocketText(final String message) {
        switch (message) {
            case "req-output-info": {
                this.session.ifPresent(session -> {
                    //TODO send output info gathered from the connector
                    session.getRemote()
                           .sendStringByFuture("output info here");
                });
                break;
            }
            case "ack-output-info": {
                this.renderPending.set(false);
                //request to render next frame
                this.html5Connector.requestPngBuffer(this);
                break;
            }
            case "ack-frame": {
                this.renderedBufferAge = this.pendingBufferAge;
                this.renderedBuffer = this.pendingBuffer;

                this.pendingBuffer = Optional.empty();
                this.pendingBufferAge = 0L;

                this.renderPending.set(false);

                //request to render next frame
                this.html5Connector.requestPngBuffer(this);
                break;
            }
            default: {
                this.jobExecutor.submit(() -> this.html5Seat.handle(message));
            }
        }
    }


    @Override
    public void onWebSocketClose(final int statusCode,
                                 final String reason) {
        this.session = Optional.empty();
        this.html5Connector.onWebSocketClose(this);
    }

    @Override
    public void onWebSocketConnect(final Session session) {
        session.getRemote()
               .setBatchMode(BatchMode.OFF);
        this.session = Optional.of(session);
        this.html5Connector.onWebSocketConnect(this);
    }

    @Override
    public void onWebSocketError(final Throwable cause) {
        //TODO log & handle
        cause.printStackTrace();
    }

    public AtomicBoolean getRenderPending() {
        return this.renderPending;
    }
}
