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
import com.google.common.collect.Lists;

import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.EventLoop;
import org.freedesktop.wayland.server.EventSource;
import org.freedesktop.wayland.server.WlSurfaceResource;

import java.util.LinkedList;
import java.util.Optional;

import javax.annotation.Nonnull;

//TODO lot's of low hanging optimizations here.
@AutoFactory(className = "CompositorFactory")
public class Compositor {

    @Nonnull
    private final Display display;
    @Nonnull
    private final GLDrawables glDrawables;
    @Nonnull
    private final ShmRenderer shmRenderer;
    @Nonnull
    private final LinkedList<WlSurfaceResource> surfacesStack = Lists.newLinkedList();
    @Nonnull
    private final EventLoop.IdleHandler idleHandler;
    @Nonnull
    private Optional<EventSource> renderEvent = Optional.empty();

    Compositor(@Nonnull @Provided final Display display,
               @Nonnull @Provided final GLDrawables glDrawables,
               @Nonnull final ShmRenderer shmRenderer) {
        this.display = display;
        this.glDrawables = glDrawables;
        this.shmRenderer = shmRenderer;
        this.idleHandler = this::handleIdle;
    }

    private void handleIdle() {
        this.renderEvent.get()
                        .remove();
        this.renderEvent = Optional.empty();

        this.glDrawables.get().forEach(glDrawable -> {
            this.shmRenderer.beginRender(glDrawable);
            getSurfacesStack().forEach(this.shmRenderer::render);
            this.shmRenderer.endRender(glDrawable);
        });

        this.display.flushClients();
    }

    public void requestRender() {
        if (!this.renderEvent.isPresent() && needsRender()) {
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

    private boolean needsRender() {
//        final WlSurfaceRequests implementation = surfaceResource.getImplementation();
//        final Surface Surface = ((WlSurface) implementation).getSurface();
//        if (Surface.isDestroyed()) {
//            return true;
//        }
//        else {
        //for now, always redraw
        return true;
//        }
    }
}
