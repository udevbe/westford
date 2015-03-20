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
import org.freedesktop.wayland.server.ShmBuffer;
import org.freedesktop.wayland.server.WlBufferResource;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.westmalle.wayland.protocol.WlSurface;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@AutoFactory(className = "ShmRendererFactory")
public class ShmRenderer {

    private final ShmRenderEngine shmRenderEngine;

    ShmRenderer(final ShmRenderEngine shmRenderEngine) {
        this.shmRenderEngine = shmRenderEngine;
    }

    public void render(final WlSurfaceResource surfaceResource) {
        final WlSurface implementation = (WlSurface) surfaceResource.getImplementation();
        final WlBufferResource wlBufferResource = implementation.getSurface()
                                                                .getState()
                                                                .getBuffer()
                                                                .get();
        try {
            this.shmRenderEngine.draw(surfaceResource,
                                      wlBufferResource)
                                .get();
        }
        catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void beginRender() throws ExecutionException, InterruptedException {
        this.shmRenderEngine.begin()
                            .get();
    }

    public void endRender() throws ExecutionException, InterruptedException {
        this.shmRenderEngine.end()
                            .get();
    }
}
