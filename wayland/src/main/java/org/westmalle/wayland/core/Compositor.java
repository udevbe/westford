//Copyright 2015 Erik De Rijcke
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
package org.westmalle.wayland.core;

import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.EventLoop;
import org.freedesktop.wayland.server.EventSource;
import org.westmalle.wayland.protocol.WlOutput;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Singleton
public class Compositor {

    @Nonnull
    private final Display  display;
    @Nonnull
    private final Renderer renderer;
    @Nonnull
    private final LinkedList<WlOutput> wlOutputs = new LinkedList<>();
    @Nonnull
    private final EventLoop.IdleHandler idleHandler;

    @Nonnull
    private Optional<EventSource> renderEvent = Optional.empty();

    @Inject
    Compositor(@Nonnull final Display display,
               @Nonnull final Renderer renderer) {
        this.display = display;
        this.renderer = renderer;
        this.idleHandler = this::handleIdle;
    }

    private void handleIdle() {
        this.renderEvent.get()
                        .remove();
        this.renderEvent = Optional.empty();
        //TODO unit test with subsurfaces render order
        //TODO unit test with parent surface without buffer while clients have a buffer.
        this.wlOutputs.forEach(this.renderer::render);
        this.display.flushClients();
    }

    public void requestRender() {
        if (!this.renderEvent.isPresent()) {
            renderScene();
        }
    }

    private void renderScene() {
        this.renderEvent = Optional.of(this.display.getEventLoop()
                                                   .addIdle(this.idleHandler));
    }

    @Nonnegative
    public int getTime() {
        return (int) TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
    }

    @Nonnull
    public LinkedList<WlOutput> getWlOutputs() {
        return this.wlOutputs;
    }
}
