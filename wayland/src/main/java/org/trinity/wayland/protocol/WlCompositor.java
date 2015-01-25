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
package org.trinity.wayland.protocol;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import com.google.common.collect.Sets;
import org.freedesktop.wayland.server.*;
import org.trinity.wayland.output.Compositor;
import org.trinity.wayland.output.Surface;
import org.trinity.wayland.output.SurfaceConfigurable;

import javax.inject.Inject;
import java.util.Set;

@AutoFactory(className = "WlCompositorFactory")
public class WlCompositor extends Global<WlCompositorResource> implements WlCompositorRequestsV3, ProtocolObject<WlCompositorResource> {

    private final Set<WlCompositorResource> resources = Sets.newHashSet();

    private final WlSurfaceFactory                         wlSurfaceFactory;
    private final WlRegionFactory                          wlRegionFactory;
    private final org.trinity.wayland.output.RegionFactory pixmanRegionFactory;
    private final Compositor                               compositor;

    @Inject
    WlCompositor(@Provided final Display display,
                 @Provided final WlSurfaceFactory wlSurfaceFactory,
                 @Provided final WlRegionFactory wlRegionFactory,
                 @Provided final org.trinity.wayland.output.RegionFactory pixmanRegionFactory,
                 final Compositor compositor) {
        super(display,
              WlCompositorResource.class,
              VERSION);
        this.wlSurfaceFactory = wlSurfaceFactory;
        this.wlRegionFactory = wlRegionFactory;
        this.pixmanRegionFactory = pixmanRegionFactory;
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
        final Surface surface = this.compositor.create();
        final WlSurface wlSurface = this.wlSurfaceFactory.create(compositorResource,
                                                                 surface);

        final WlSurfaceResource surfaceResource = wlSurface.add(compositorResource.getClient(),
                                                                compositorResource.getVersion(),
                                                                id);
        surfaceResource.addDestroyListener(new Listener() {
            @Override
            public void handle() {
                remove();
                WlCompositor.this.compositor.getScene()
                                            .getSurfacesStack()
                                            .remove(surfaceResource);
                surface.accept(SurfaceConfigurable::markDestroyed);
                WlCompositor.this.compositor.requestRender(surfaceResource);
            }
        });

        this.compositor.getScene()
                       .getSurfacesStack()
                       .push(surfaceResource);
    }

    @Override
    public void createRegion(final WlCompositorResource resource,
                             final int id) {
        this.wlRegionFactory.create(this.pixmanRegionFactory.create())
                            .add(resource.getClient(),
                                 resource.getVersion(),
                                 id);
    }

    @Override
    public Set<WlCompositorResource> getResources() {
        return this.resources;
    }

    @Override
    public WlCompositorResource create(final Client client,
                                       final int version,
                                       final int id) {
        return new WlCompositorResource(client,
                                        version,
                                        id,
                                        this);
    }

    public Compositor getCompositor() {
        return this.compositor;
    }
}
