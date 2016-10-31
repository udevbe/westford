/*
 * Westmalle Wayland Compositor.
 * Copyright (C) 2016  Erik De Rijcke
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.westmalle.compositor.protocol;

import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.Global;
import org.freedesktop.wayland.server.WlSubcompositorRequests;
import org.freedesktop.wayland.server.WlSubcompositorResource;
import org.freedesktop.wayland.server.WlSubsurfaceResource;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.freedesktop.wayland.shared.WlSubcompositorError;
import org.westmalle.compositor.core.Role;
import org.westmalle.compositor.core.Subsurface;
import org.westmalle.compositor.core.SubsurfaceFactory;
import org.westmalle.compositor.core.Surface;
import org.westmalle.compositor.protocol.WlSubsurfaceFactory;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;

@Singleton
public class WlSubcompositor extends Global<WlSubcompositorResource> implements WlSubcompositorRequests, ProtocolObject<WlSubcompositorResource> {

    private final Set<WlSubcompositorResource> resources = Collections.newSetFromMap(new WeakHashMap<>());

    private final WlSubsurfaceFactory wlSubSurfaceFactory;
    private final SubsurfaceFactory   subsurfaceFactory;

    @Inject
    WlSubcompositor(@Nonnull final Display display,
                    @Nonnull final WlSubsurfaceFactory wlSubSurfaceFactory,
                    @Nonnull final SubsurfaceFactory subsurfaceFactory) {
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

        final Optional<Role> role    = surface.getRole();
        final boolean        hasRole = role.isPresent();

        /*
         * Check if the surface does not have a role or has an inactive subsurface role, both are ok. Otherwise we raise
         * a protocol error.
         */
        if (!hasRole ||
            role.get() instanceof Subsurface &&
            ((Subsurface) role.get()).isInert()) {

            final Subsurface subsurface = this.subsurfaceFactory.create(parentWlSurfaceResource,
                                                                        wlSurfaceResource);
            surface.setRole(subsurface);

            final WlSubsurface wlSubsurface = this.wlSubSurfaceFactory.create(subsurface);
            final WlSubsurfaceResource wlSubsurfaceResource = wlSubsurface.add(requester.getClient(),
                                                                               requester.getVersion(),
                                                                               id);
            wlSurfaceResource.register(wlSubsurfaceResource::destroy);
        }
        else {
            requester.getClient()
                     .getObject(Display.OBJECT_ID)
                     .postError(WlSubcompositorError.BAD_SURFACE.value,
                                String.format("Desired sub surface already has another role (%s)",
                                              role.get()
                                                  .getClass()
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
