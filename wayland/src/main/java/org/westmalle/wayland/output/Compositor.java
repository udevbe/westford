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
package org.westmalle.wayland.output;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.EventLoop;
import org.freedesktop.wayland.server.EventSource;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.westmalle.wayland.protocol.WlOutput;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.Optional;

//TODO lot's of low hanging optimizations here.
@AutoFactory(className = "CompositorFactory")
public class Compositor {

    @Nonnull
    private final Display     display;
    @Nonnull
    private final Renderer renderer;
    @Nonnull
    private final LinkedList<WlSurfaceResource> surfacesStack = new LinkedList<>();
    @Nonnull
    private final LinkedList<WlOutput> wlOutputs = new LinkedList<>();
    @Nonnull
    private final EventLoop.IdleHandler idleHandler;
    @Nonnull
    private Optional<EventSource> renderEvent = Optional.empty();

    Compositor(@Nonnull @Provided final Display display,
               @Nonnull final Renderer renderer) {
        this.display = display;
        this.renderer = renderer;
        this.idleHandler = this::handleIdle;
    }

    private void handleIdle() {
        this.renderEvent.get()
                        .remove();
        this.renderEvent = Optional.empty();

        this.wlOutputs.forEach(wlOutput -> {
            final Object outputImplementation = wlOutput.getOutput().getImplementation();
            this.renderer.beginRender(outputImplementation);
            getSurfacesStack().forEach(this.renderer::render);
            this.renderer.endRender(outputImplementation);
        });

        this.display.flushClients();
    }

    public void requestRender() {
        if (!this.renderEvent.isPresent()) {
            renderScene();
        }
    }

    public void renderScene() {
        this.renderEvent = Optional.of(this.display.getEventLoop()
                                                   .addIdle(this.idleHandler));
    }

    @Nonnull
    public LinkedList<WlSurfaceResource> getSurfacesStack() {
        return this.surfacesStack;
    }

    @Nonnull
    public LinkedList<WlOutput> getWlOutputs() {
        return this.wlOutputs;
    }
}
