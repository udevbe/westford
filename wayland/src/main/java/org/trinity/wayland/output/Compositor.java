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
package org.trinity.wayland.output;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.WlSurfaceResource;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

@AutoFactory(className = "CompositorFactory")
public class Compositor {

    private final Display        display;
    private final Scene          scene;
    private final ShmRenderer    shmRenderer;
    private final SurfaceFactory simpleShellSurfaceFactory;

    private final AtomicBoolean renderScheduled = new AtomicBoolean(false);

    Compositor(@Provided final Display display,
               @Provided final Scene scene,
               final ShmRenderer shmRenderer,
               @Provided final SurfaceFactory simpleShellSurfaceFactory) {
        this.display = display;
        this.scene = scene;
        this.shmRenderer = shmRenderer;
        this.simpleShellSurfaceFactory = simpleShellSurfaceFactory;
    }

    public Surface create() {
        final Surface shellSurface = this.simpleShellSurfaceFactory.create(Optional.empty());
        shellSurface.register(this);
        return shellSurface;
    }

    public void requestRender(final WlSurfaceResource surfaceResource) {
        if (this.renderScheduled.compareAndSet(false,
                                               true)) {
            if (this.scene.needsRender(surfaceResource)) {
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
                                this.scene.getSurfacesStack()
                                          .forEach(this.shmRenderer::render);
                                this.shmRenderer.endRender();
                                this.display.flushClients();
                            }
                            catch (ExecutionException | InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    });
    }

    public Scene getScene() {
        return this.scene;
    }
}
