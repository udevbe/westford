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
import org.freedesktop.wayland.server.WlBufferResource;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.westmalle.wayland.protocol.WlOutput;
import org.westmalle.wayland.protocol.WlSurface;

import javax.annotation.Nonnull;
import java.util.Optional;

@AutoFactory(className = "RendererFactory")
public class Renderer {

    @Nonnull
    private final RenderEngine renderEngine;

    Renderer(@Nonnull final RenderEngine renderEngine) {
        this.renderEngine = renderEngine;
    }

    public void render(@Nonnull final WlSurfaceResource surfaceResource) {
        final WlSurface wlSurface = (WlSurface) surfaceResource.getImplementation();
        final Optional<WlBufferResource> buffer = wlSurface.getSurface()
                                                           .getState()
                                                           .getBuffer();
        buffer.ifPresent(wlBufferResource -> this.renderEngine.draw(surfaceResource,
                                                                    wlBufferResource));
    }

    public void beginRender(@Nonnull final WlOutput wlOutput) {
        this.renderEngine.begin(wlOutput);
    }

    public void endRender(@Nonnull final WlOutput wlOutput) {
        this.renderEngine.end(wlOutput);
    }
}
