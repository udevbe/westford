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
import org.freedesktop.wayland.server.WlSubcompositorRequests;
import org.freedesktop.wayland.server.WlSubcompositorResource;
import org.freedesktop.wayland.server.WlSubsurfaceResource;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.freedesktop.wayland.shared.WlSubcompositorError;
import org.westmalle.wayland.core.Role;
import org.westmalle.wayland.core.Subsurface;
import org.westmalle.wayland.core.SubsurfaceFactory;
import org.westmalle.wayland.core.Surface;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;

@AutoFactory(className = "WlSubcompositorFactory")
public class WlSubcompositor extends Global<WlSubcompositorResource> implements WlSubcompositorRequests, ProtocolObject<WlSubcompositorResource> {

    private final Set<WlSubcompositorResource> resources = Collections.newSetFromMap(new WeakHashMap<>());

    private final WlSubsurfaceFactory wlSubSurfaceFactory;
    private final SubsurfaceFactory   subsurfaceFactory;

    private final Set<Subsurface> activeSubsurfaceRoles = new HashSet<>();

    WlSubcompositor(@Provided final Display display,
                    @Provided final WlSubsurfaceFactory wlSubSurfaceFactory,
                    @Provided final org.westmalle.wayland.core.SubsurfaceFactory subsurfaceFactory) {
        super(display,
              WlSubcompositorResource.class,
              VERSION);
        this.wlSubSurfaceFactory = wlSubSurfaceFactory;
        this.subsurfaceFactory = subsurfaceFactory;
    }

    @Override
    public void destroy(final WlSubcompositorResource resource) {
        resource.destroy();
    }

    @Override
    public void getSubsurface(final WlSubcompositorResource requester,
                              final int id,
                              @Nonnull final WlSurfaceResource wlSurfaceResource,
                              @Nonnull final WlSurfaceResource parentWlSurfaceResource) {

        final WlSurface wlSurface = (WlSurface) wlSurfaceResource.getImplementation();
        final Surface   surface   = wlSurface.getSurface();

        final Role role = surface.getRole()
                                 .orElseGet(() -> this.subsurfaceFactory.create(parentWlSurfaceResource,
                                                                                wlSurfaceResource));
        if (role instanceof Subsurface &&
            !this.activeSubsurfaceRoles.contains(role)) {

            surface.setRole(role);

            final Subsurface subsurface = (Subsurface) role;
            final WlSubsurface wlSubsurface = this.wlSubSurfaceFactory.create(subsurface);
            final WlSubsurfaceResource wlSubsurfaceResource = wlSubsurface.add(requester.getClient(),
                                                                               requester.getVersion(),
                                                                               id);
            this.activeSubsurfaceRoles.add(subsurface);

            wlSubsurfaceResource.register(() -> this.activeSubsurfaceRoles.remove(subsurface));
            wlSurfaceResource.register(wlSubsurfaceResource::destroy);
        }
        else {
            requester.getClient()
                     .getObject(Display.OBJECT_ID)
                     .postError(WlSubcompositorError.BAD_SURFACE.getValue(),
                                String.format("Desired sub surface already has another role (%s)",
                                              role.getClass()
                                                  .getSimpleName()));
        }
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

    @Nonnull
    @Override
    public Set<WlSubcompositorResource> getResources() {
        return this.resources;
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
