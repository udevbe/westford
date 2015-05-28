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
import org.freedesktop.wayland.server.WlBufferResource;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.westmalle.wayland.protocol.WlSurface;

import javax.annotation.Nonnull;
import java.util.concurrent.ExecutionException;

@AutoFactory(className = "RendererFactory")
public class Renderer {

    @Nonnull
    private final RenderEngine renderEngine;

    Renderer(@Nonnull final RenderEngine renderEngine) {
        this.renderEngine = renderEngine;
    }

    public void render(@Nonnull final WlSurfaceResource surfaceResource) {
        final WlSurface wlSurface = (WlSurface) surfaceResource.getImplementation();
        final WlBufferResource wlBufferResource = wlSurface.getSurface()
                                                           .getState()
                                                           .getBuffer()
                                                           .get();
        this.renderEngine.draw(surfaceResource,
                               wlBufferResource);
    }

    public void beginRender(@Nonnull final Object outputImplementation) {
        this.renderEngine.begin(outputImplementation);
    }

    public void endRender(@Nonnull final Object outputImplementation) {
        try {
            //wait for rendering to finish
            this.renderEngine.end(outputImplementation)
                             .get();
        }
        catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
