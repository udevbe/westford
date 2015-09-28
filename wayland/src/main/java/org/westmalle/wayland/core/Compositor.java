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

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.EventLoop;
import org.freedesktop.wayland.server.EventSource;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.westmalle.wayland.protocol.WlOutput;
import org.westmalle.wayland.protocol.WlSurface;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@AutoFactory(className = "CompositorFactory",
             allowSubclasses = true)
public class Compositor {
    @Nonnull
    private final Display      display;
    @Nonnull
    private final RenderEngine renderEngine;
    @Nonnull
    private final LinkedList<WlOutput> wlOutputs = new LinkedList<>();
    @Nonnull
    private final EventLoop.IdleHandler idleHandler;
    @Nonnull
    private final LinkedList<WlSurfaceResource> surfacesStack = new LinkedList<>();
    @Nonnull
    private final Map<WlSurfaceResource, LinkedList<WlSurfaceResource>> subsurfaceStack        = new HashMap<>();
    @Nonnull
    private final Map<WlSurfaceResource, LinkedList<WlSurfaceResource>> pendingSubsurfaceStack = new HashMap<>();
    @Nonnull
    private Optional<EventSource> renderEvent = Optional.empty();

    Compositor(@Nonnull @Provided final Display display,
               @Nonnull @Provided final RenderEngine renderEngine) {
        this.display = display;
        this.renderEngine = renderEngine;
        this.idleHandler = this::handleIdle;
    }

    private void handleIdle() {
        this.renderEvent.get()
                        .remove();
        this.renderEvent = Optional.empty();
        //TODO unit test with subsurfaces render order
        //TODO unit test with parent surface without buffer while clients have a buffer.
        this.wlOutputs.forEach(this::render);
        this.display.flushClients();
    }

    private void render(@Nonnull final WlOutput wlOutput) {
        this.renderEngine.begin(wlOutput);
        getSurfacesStack().forEach(this::render);
        this.renderEngine.end(wlOutput);
    }

    @Nonnull
    public LinkedList<WlSurfaceResource> getSurfacesStack() {
        return this.surfacesStack;
    }

    private void render(final WlSurfaceResource wlSurfaceResource) {
        final WlSurface wlSurface = (WlSurface) wlSurfaceResource.getImplementation();
        //don't bother rendering subsurfaces if the parent doesn't have a buffer.
        wlSurface.getSurface()
                 .getState()
                 .getBuffer()
                 .ifPresent(wlBufferResource -> {
                     final LinkedList<WlSurfaceResource> subsurfaces = getSubsurfaceStack(wlSurfaceResource);
                     this.renderEngine.draw(wlSurfaceResource,
                                            wlBufferResource);
                     subsurfaces.forEach((subsurface) -> {
                         if (subsurface != wlSurfaceResource) {
                             render(subsurface);
                         }
                     });
                 });
    }

    public void removeSubsurfaceStack(@Nonnull final WlSurfaceResource parentSurface) {
        this.subsurfaceStack.remove(parentSurface);
        this.pendingSubsurfaceStack.remove(parentSurface);
    }

    public void commitSubsurfaceStack(@Nonnull final WlSurfaceResource parentSurface) {
        this.subsurfaceStack.put(parentSurface,
                                 getPendingSubsurfaceStack(parentSurface));
        this.pendingSubsurfaceStack.remove(parentSurface);
    }

    /**
     * Get a pending z-ordered stack of subsurfaces grouped by their parent.
     * The returned subsurface stack is only valid until {@link #commitSubsurfaceStack(WlSurfaceResource)} is called.
     *
     * @param parentSurface the parent of the subsurfaces.
     *
     * @return A list of subsurfaces, including the parent, in z-order.
     */
    @Nonnull
    public LinkedList<WlSurfaceResource> getPendingSubsurfaceStack(@Nonnull final WlSurfaceResource parentSurface) {
        LinkedList<WlSurfaceResource> subsurfaces = this.pendingSubsurfaceStack.get(parentSurface);
        if (subsurfaces == null) {
            //TODO unit test pending subsurface stack initialization
            subsurfaces = new LinkedList<>(getSubsurfaceStack(parentSurface));
            this.pendingSubsurfaceStack.put(parentSurface,
                                            subsurfaces);
        }
        return subsurfaces;
    }

    @Nonnull
    public LinkedList<WlSurfaceResource> getSubsurfaceStack(@Nonnull final WlSurfaceResource parentSurface) {
        LinkedList<WlSurfaceResource> subsurfaces = this.subsurfaceStack.get(parentSurface);
        if (subsurfaces == null) {
            //TODO unit test subsurface stack initialization
            subsurfaces = new LinkedList<>();
            subsurfaces.add(parentSurface);
            this.subsurfaceStack.put(parentSurface,
                                     subsurfaces);
        }
        return subsurfaces;
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
