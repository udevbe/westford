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
import com.jogamp.opengl.GLDrawable;
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
        final WlSurface implementation = (WlSurface) surfaceResource.getImplementation();
        final WlBufferResource wlBufferResource = implementation.getSurface()
                                                                .getState()
                                                                .getBuffer()
                                                                .get();
        try {
            this.renderEngine.draw(surfaceResource,
                                   wlBufferResource)
                             .get();
        }
        catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void beginRender(@Nonnull final Object outputImplementation) {
        try {
            this.renderEngine.begin(outputImplementation)
                                .get();
        }
        catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void endRender(@Nonnull final Object outputImplementation) {
        try {
            this.renderEngine.end(outputImplementation)
                                .get();
        }
        catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
