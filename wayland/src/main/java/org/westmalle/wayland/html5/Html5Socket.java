//Copyright 2016 Erik De Rijcke
//
//Licensed under the Apache License,Version2.0(the"License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
//http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing,software
//distributed under the License is distributed on an"AS IS"BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
package org.westmalle.wayland.html5;


import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import org.eclipse.jetty.websocket.api.BatchMode;
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

@AutoFactory
public class Html5Socket implements WebSocketListener {

    private static final String REQUEST_OUTPUT_INFO = "roi";
    private static final String ACK_OUTPUT_INFO     = "aoi";
    private static final String ACK_FRAME           = "af";


    @Nonnull
    private final JobExecutor    jobExecutor;
    @Nonnull
    private final Html5Connector html5Connector;
    @Nonnull
    private final Html5Seat      html5Seat;

    private Optional<Session> session = Optional.empty();

    private final ScheduledExecutorService socketThread = Executors.newSingleThreadScheduledExecutor();

    private final AtomicBoolean renderPending = new AtomicBoolean(true);

    private Optional<ByteBuffer> pendingBuffer = Optional.empty();
    private long pendingBufferAge;

    private Optional<ByteBuffer> renderedBuffer = Optional.empty();
    private long renderedBufferAge;

    private long clientRenderInterval = 0;

    private Optional<ScheduledFuture<?>> scheduledFrame = Optional.empty();


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
            this.session.ifPresent(session -> {
                //TODO send output info gathered from the connector
                session.getRemote()
                       .sendStringByFuture("output info here");
            });
        }
        else if (message.equals(ACK_OUTPUT_INFO)) {
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
            this.jobExecutor.submit(() -> this.html5Seat.handle(message));
        }
    }

    private void requestFrame() {
        this.renderedBufferAge = this.pendingBufferAge;
        this.renderedBuffer = this.pendingBuffer;

        this.pendingBuffer = Optional.empty();
        this.pendingBufferAge = 0L;

        this.renderPending.set(false);
        this.html5Connector.requestPngBuffer(this);
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
