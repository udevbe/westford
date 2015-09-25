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
package org.westmalle.wayland.protocol;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.Global;
import org.freedesktop.wayland.server.WlCompositorRequestsV3;
import org.freedesktop.wayland.server.WlCompositorResource;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.westmalle.wayland.core.Compositor;
import org.westmalle.wayland.core.Surface;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

@AutoFactory(className = "WlCompositorFactory",
             allowSubclasses = true)
public class WlCompositor extends Global<WlCompositorResource> implements WlCompositorRequestsV3, ProtocolObject<WlCompositorResource> {

    private final Set<WlCompositorResource> resources = Collections.newSetFromMap(new WeakHashMap<>());

    private final WlSurfaceFactory                               wlSurfaceFactory;
    private final WlRegionFactory                                wlRegionFactory;
    private final org.westmalle.wayland.core.FiniteRegionFactory finiteRegionFactory;
    private final org.westmalle.wayland.core.SurfaceFactory      surfaceFactory;
    private final Compositor                                     compositor;

    @Inject
    WlCompositor(@Provided final Display display,
                 @Provided final WlSurfaceFactory wlSurfaceFactory,
                 @Provided final WlRegionFactory wlRegionFactory,
                 @Provided final org.westmalle.wayland.core.FiniteRegionFactory finiteRegionFactory,
                 @Provided final org.westmalle.wayland.core.SurfaceFactory surfaceFactory,
                 final Compositor compositor) {
        super(display,
              WlCompositorResource.class,
              VERSION);
        this.wlSurfaceFactory = wlSurfaceFactory;
        this.wlRegionFactory = wlRegionFactory;
        this.finiteRegionFactory = finiteRegionFactory;
        this.surfaceFactory = surfaceFactory;
        this.compositor = compositor;
    }

    @Override
    public WlCompositorResource onBindClient(final Client client,
                                             final int version,
                                             final int id) {
        return add(client,
                   version,
                   id);
    }

    @Override
    public void createSurface(final WlCompositorResource compositorResource,
                              final int id) {
        final Surface   surface   = this.surfaceFactory.create(compositorResource);
        final WlSurface wlSurface = this.wlSurfaceFactory.create(surface);

        final WlSurfaceResource wlSurfaceResource = wlSurface.add(compositorResource.getClient(),
                                                                  compositorResource.getVersion(),
                                                                  id);
        //TODO unit test destroy handler
        wlSurfaceResource.register(() -> {
            this.compositor.getSurfacesStack()
                           .remove(wlSurfaceResource);
            this.compositor.removeSubsurfaceStack(wlSurfaceResource);
            surface.markDestroyed();
            this.compositor.requestRender();
        });

        this.compositor.getSurfacesStack()
                       .addLast(wlSurfaceResource);

        //TODO unit test commit handler
        surface.getApplySurfaceStateSignal()
               .connect(event -> this.compositor.commitSubsurfaceStack(wlSurfaceResource));
    }

    @Override
    public void createRegion(final WlCompositorResource resource,
                             final int id) {
        this.wlRegionFactory.create(this.finiteRegionFactory.create())
                            .add(resource.getClient(),
                                 resource.getVersion(),
                                 id);
    }

    @Nonnull
    @Override
    public WlCompositorResource create(@Nonnull final Client client,
                                       @Nonnegative final int version,
                                       final int id) {
        return new WlCompositorResource(client,
                                        version,
                                        id,
                                        this);
    }

    @Nonnull
    @Override
    public Set<WlCompositorResource> getResources() {
        return this.resources;
    }

    public Compositor getCompositor() {
        return this.compositor;
    }
}
