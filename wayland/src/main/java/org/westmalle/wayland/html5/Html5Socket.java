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
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.westmalle.wayland.core.JobExecutor;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

@AutoFactory
public class Html5Socket implements WebSocketListener {

    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    private static final String REQUEST_OUTPUT_INFO = "roi";
    private static final String ACK_OUTPUT_INFO     = "aoi";
    private static final String ACK_FRAME           = "af";


    @Nonnull
    private final JobExecutor       jobExecutor;
    @Nonnull
    private final Html5RenderOutput html5RenderOutput;
    @Nonnull
    private final Html5SeatFactory  html5SeatFactory;
    private final ScheduledExecutorService socketThread  = Executors.newSingleThreadScheduledExecutor();
    private final AtomicBoolean            renderPending = new AtomicBoolean(true);
    @Nonnull
    private       Optional<Html5Seat>      html5SeatOptional = Optional.empty();
    private       Optional<Session>        session       = Optional.empty();
    private       Optional<ByteBuffer>     pendingBuffer = Optional.empty();
    private long pendingBufferAge;

    private Optional<ByteBuffer> renderedBuffer = Optional.empty();
    private long renderedBufferAge;

    private long clientRenderInterval = 0;

    private Optional<ScheduledFuture<?>> scheduledFrame = Optional.empty();


    Html5Socket(@Provided @Nonnull final JobExecutor jobExecutor,
                @Provided @Nonnull final Html5SeatFactory html5SeatFactory,
                @Nonnull final Html5RenderOutput html5RenderOutput) {
        this.jobExecutor = jobExecutor;
        this.html5SeatFactory = html5SeatFactory;
        this.html5RenderOutput = html5RenderOutput;
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
                    LOGGER.severe("Failed to send png buffer using session: " + session);
                    LOGGER.throwing(Html5Socket.class.getSimpleName(),
                                    "handlePngBuffer",
                                    e);
                }
            });

            //check if this is the first render
            if (this.scheduledFrame.isPresent()) {
                //TODO we should be smart and not schedule frame sending if we haven't received a frame ack in a long time

                //schedule next frame send
                this.scheduledFrame = Optional.of(this.socketThread.schedule(this::requestFrame,
                                                                             this.clientRenderInterval,
                                                                             TimeUnit.MILLISECONDS));
            }
        }));
    }

    //we offload all incoming events from the web server thread to our own main thread.
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

        if (message.equals(REQUEST_OUTPUT_INFO)) {
            this.html5RenderOutput.requestOutputInfo(this);
        }
        else if (message.equals(ACK_OUTPUT_INFO)) {

            //TODO create seat based on authorized seat request (separate message)
            this.html5SeatOptional = Optional.of(this.html5SeatFactory.create());

            requestFrame();
        }
        else if (message.startsWith(ACK_FRAME)) {
            final String frameDrawTime = message.substring(ACK_FRAME.length());
            //use ack to throttle frame sending to client
            this.clientRenderInterval = Long.parseLong(frameDrawTime);

            //check if our latency went down and adjust accordingly
            if (this.scheduledFrame.isPresent()) {
                if (!this.scheduledFrame.get()
                                        .isDone()) {
                    this.scheduledFrame.get()
                                       .cancel(false);
                    requestFrame();
                }
            }
            else {
                requestFrame();
            }
        }
        else {
            this.jobExecutor.submit(() -> this.html5SeatOptional.ifPresent(html5Seat -> html5Seat.handle(message)));
        }
    }

    private void requestFrame() {
        this.renderedBufferAge = this.pendingBufferAge;
        this.renderedBuffer = this.pendingBuffer;

        this.pendingBuffer = Optional.empty();
        this.pendingBufferAge = 0L;

        this.renderPending.set(false);
        this.html5RenderOutput.requestPngBuffer(this);
    }


    @Override
    public void onWebSocketClose(final int statusCode,
                                 final String reason) {
        this.session = Optional.empty();
        this.html5RenderOutput.onWebSocketClose(this);
    }

    @Override
    public void onWebSocketConnect(final Session session) {
        this.session = Optional.of(session);
        this.html5RenderOutput.onWebSocketConnect(this);
    }

    @Override
    public void onWebSocketError(final Throwable cause) {
        LOGGER.warning("Got web socket error: " + cause.getMessage());
        LOGGER.throwing(Html5Socket.class.getName(),
                        "onWebSocketError",
                        cause);
        //TODO handle (remove wayland seat, reset all state)
    }

    public AtomicBoolean getRenderPending() {
        return this.renderPending;
    }

    public void handleOutputInfo(final String outputInfo) {
        this.socketThread.submit(() -> this.session.ifPresent(socketSession -> {
            try {
                socketSession.getRemote()
                             .sendString(outputInfo);
            }
            catch (final IOException e) {
                LOGGER.severe("Failed to send output info using session: " + socketSession);
                LOGGER.throwing(Html5Socket.class.getSimpleName(),
                                "handleOutputInfo",
                                e);
            }
        }));
    }
}
