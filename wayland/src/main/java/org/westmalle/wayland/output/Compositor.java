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
import org.freedesktop.wayland.server.WlSurfaceResource;

import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

@AutoFactory(className = "CompositorFactory")
public class Compositor {

    private final Display     display;
    private final ShmRenderer shmRenderer;

    private final AtomicBoolean renderScheduled = new AtomicBoolean(false);

    private final LinkedList<WlSurfaceResource> surfacesStack = Lists.newLinkedList();


    Compositor(@Provided final Display display,
               final ShmRenderer shmRenderer) {
        this.display = display;
        this.shmRenderer = shmRenderer;
    }

    public void requestRender() {
        if (this.renderScheduled.compareAndSet(false,
                                               true)) {
            if (needsRender()) {
                renderScene();
            }
        }
    }

    private void renderScene() {
        this.display.getEventLoop()
                    .addIdle(() -> {
                        if (this.renderScheduled.compareAndSet(true,
                                                               false)) {
                            try {
                                this.shmRenderer.beginRender();
                                getSurfacesStack().forEach(this.shmRenderer::render);
                                this.shmRenderer.endRender();
                                this.display.flushClients();
                            }
                            catch (ExecutionException | InterruptedException e) {
                                //TODO proper error handling
                                e.printStackTrace();
                            }
                        }
                    });
    }

    public LinkedList<WlSurfaceResource> getSurfacesStack() { return this.surfacesStack; }

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
