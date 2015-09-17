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

import org.freedesktop.wayland.server.DestroyListener;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.westmalle.wayland.protocol.WlCompositor;
import org.westmalle.wayland.protocol.WlSurface;

import javax.annotation.Nonnull;

public class SubsurfaceFactory {

    public Subsurface create(@Nonnull final WlSurfaceResource parentWlSurfaceResource,
                             @Nonnull final WlSurfaceResource wlSurfaceResource) {

        final WlSurface wlSurface = (WlSurface) wlSurfaceResource.getImplementation();
        final Surface   surface   = wlSurface.getSurface();

        final Subsurface subsurface = new Subsurface(parentWlSurfaceResource,
                                                     wlSurfaceResource,
                                                     surface.getState());
        surface.getCommitSignal()
               .connect(event -> subsurface.commit());

        final WlSurface parentWlSurface = (WlSurface) parentWlSurfaceResource.getImplementation();
        final Surface   parentSurface   = parentWlSurface.getSurface();

        parentSurface.getCommitSignal()
                     .connect(event -> subsurface.parentCommit());
        parentSurface.getPositionSignal()
                     .connect(event -> subsurface.applyPosition());

        final WlCompositor wlCompositor = (WlCompositor) surface.getWlCompositorResource()
                                                                .getImplementation();
        final Compositor compositor = wlCompositor.getCompositor();
        compositor.getSurfacesStack()
                  .remove(wlSurfaceResource);
        compositor.getSubsurfaceStack(parentWlSurfaceResource)
                  .addLast(wlSurfaceResource);

        final DestroyListener destroyListener = () -> {
            compositor.getSubsurfaceStack(parentWlSurfaceResource)
                      .remove(wlSurfaceResource);
            compositor.getPendingSubsurfaceStack(parentWlSurfaceResource)
                      .remove(wlSurfaceResource);
        };
        wlSurfaceResource.register(destroyListener);

        parentWlSurfaceResource.register(() -> {
            /*
             * A destroyed parent will have it's stack of subsurfaces removed, so no need to remove the subsurface
             * from that stack (which is done in the subsurface destroy listener).
             */
            wlSurfaceResource.unregister(destroyListener);
        });

        return subsurface;
    }
}
