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

import org.freedesktop.wayland.server.WlBufferResource;
import org.freedesktop.wayland.server.WlSurfaceResource;

import javax.annotation.Nonnull;
import java.util.Optional;

public interface Renderer {
    void visit(@Nonnull Connector connector);

    void visit(@Nonnull EglConnector eglConnector);

    //TODO pixman sw rendering platform
    //void visit(PixmanPlatform pixmanPlatform);
    
    /**
     * @param wlSurfaceResource
     *
     * @deprecated method will be removed
     */
    //FIXME remove this method and instead register a destroy listener in the renderer implementation
    @Deprecated
    void onDestroy(@Nonnull WlSurfaceResource wlSurfaceResource);

    @Nonnull
    Buffer queryBuffer(@Nonnull WlBufferResource wlBufferResource);
}
