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

import com.google.common.collect.Sets;
import org.freedesktop.wayland.server.*;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Set;
import java.util.WeakHashMap;

@Singleton
public class WlSubCompositor extends Global<WlSubcompositorResource> implements WlSubcompositorRequests, ProtocolObject<WlSubcompositorResource> {

    private final Set<WlSubcompositorResource> resources = Sets.newSetFromMap(new WeakHashMap<>());

    private final WlSubSurfaceFactory wlSubSurfaceFactory;

    @Inject
    WlSubCompositor(final Display display,
                    final WlSubSurfaceFactory wlSubSurfaceFactory) {
        super(display,
              WlSubcompositorResource.class,
              VERSION);
        this.wlSubSurfaceFactory = wlSubSurfaceFactory;
    }

    @Override
    public void destroy(final WlSubcompositorResource resource) {
        resource.destroy();
    }

    @Override
    public void getSubsurface(final WlSubcompositorResource requester,
                              final int id,
                              @Nonnull final WlSurfaceResource surface,
                              @Nonnull final WlSurfaceResource parent) {
        //TODO check if surface doesn't already have a role
        final WlSubsurfaceResource wlSubsurfaceResource = this.wlSubSurfaceFactory.create(surface,
                                                                                          parent)
                                                                                  .add(requester.getClient(),
                                                                                       requester.getVersion(),
                                                                                       id);
        surface.addDestroyListener(new Listener() {
            @Override
            public void handle() {
                remove();
                wlSubsurfaceResource.destroy();
            }
        });
    }

    @Nonnull
    @Override
    public Set<WlSubcompositorResource> getResources() {
        return this.resources;
    }

    @Nonnull
    @Override
    public WlSubcompositorResource create(@Nonnull final Client client,
                                          @Nonnegative final int version,
                                          final int id) {
        return new WlSubcompositorResource(client,
                                           version,
                                           id,
                                           this);
    }

    @Override
    public WlSubcompositorResource onBindClient(final Client client,
                                                final int version,
                                                final int id) {
        return new WlSubcompositorResource(client,
                                           version,
                                           id,
                                           this);
    }
}
