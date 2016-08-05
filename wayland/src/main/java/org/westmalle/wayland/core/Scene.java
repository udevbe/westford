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
package org.westmalle.wayland.core;

import org.freedesktop.wayland.server.WlSurfaceRequests;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.westmalle.wayland.protocol.WlSurface;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

@Singleton
public class Scene {
    @Nonnull
    private final LinkedList<WlSurfaceResource>                         surfacesStack          = new LinkedList<>();
    @Nonnull
    private final Map<WlSurfaceResource, LinkedList<WlSurfaceResource>> subsurfaceStack        = new HashMap<>();
    @Nonnull
    private final Map<WlSurfaceResource, LinkedList<WlSurfaceResource>> pendingSubsurfaceStack = new HashMap<>();
    @Nonnull
    private final InfiniteRegion infiniteRegion;

    @Inject
    Scene(@Nonnull final InfiniteRegion infiniteRegion) {
        this.infiniteRegion = infiniteRegion;
    }

    @Nonnull
    public LinkedList<WlSurfaceResource> getSurfacesStack() {
        return this.surfacesStack;
    }

    public void removeSubsurfaceStack(@Nonnull final WlSurfaceResource parentSurface) {
        this.subsurfaceStack.remove(parentSurface);
        this.pendingSubsurfaceStack.remove(parentSurface);
    }

    /**
     * Get a pending z-ordered stack of subsurfaces grouped by their parent.
     * The returned subsurface stack is only valid until {@link #commitSubsurfaceStack(WlSurfaceResource)} is called.
     *
     * @param parentSurface the parent of the subsurfaces.
     *
     * @return A list of subsurfaces, including the parent, in z-order.
     */
    @Nonnull
    public LinkedList<WlSurfaceResource> getPendingSubsurfaceStack(@Nonnull final WlSurfaceResource parentSurface) {
        LinkedList<WlSurfaceResource> subsurfaces = this.pendingSubsurfaceStack.get(parentSurface);
        if (subsurfaces == null) {
            //TODO unit test pending subsurface stack initialization
            subsurfaces = new LinkedList<>(getSubsurfaceStack(parentSurface));
            this.pendingSubsurfaceStack.put(parentSurface,
                                            subsurfaces);
        }
        return subsurfaces;
    }

    @Nonnull
    public LinkedList<WlSurfaceResource> getSubsurfaceStack(@Nonnull final WlSurfaceResource parentSurface) {
        LinkedList<WlSurfaceResource> subsurfaces = this.subsurfaceStack.get(parentSurface);
        if (subsurfaces == null) {
            //TODO unit test subsurface stack initialization
            subsurfaces = new LinkedList<>();
            subsurfaces.add(parentSurface);
            this.subsurfaceStack.put(parentSurface,
                                     subsurfaces);
        }
        return subsurfaces;
    }

    public void commitSubsurfaceStack(@Nonnull final WlSurfaceResource parentSurface) {
        this.subsurfaceStack.put(parentSurface,
                                 getPendingSubsurfaceStack(parentSurface));
        this.pendingSubsurfaceStack.remove(parentSurface);
    }

    @Nonnull
    public Optional<WlSurfaceResource> pickSurface(final Point global) {
        final Iterator<WlSurfaceResource> surfaceIterator = getSurfacesStack().descendingIterator();
        Optional<WlSurfaceResource>       pointerOver     = Optional.empty();
        while (surfaceIterator.hasNext()) {
            final WlSurfaceResource surfaceResource = surfaceIterator.next();
            final WlSurfaceRequests implementation  = surfaceResource.getImplementation();
            final Surface           surface         = ((WlSurface) implementation).getSurface();

            //surface can be invisible (null buffer), in which case we should ignore it.
            if (!surface.getState()
                        .getBuffer()
                        .isPresent()) {
                continue;
            }

            final Optional<Region> inputRegion = surface.getState()
                                                        .getInputRegion();
            final Region region = inputRegion.orElseGet(() -> this.infiniteRegion);

            final Rectangle size  = surface.getSize();
            final Point     local = surface.local(global);
            if (region.contains(size,
                                local)) {
                pointerOver = Optional.of(surfaceResource);
                break;
            }
        }

        return pointerOver;
    }
}
