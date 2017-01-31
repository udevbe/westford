/*
 * Westford Wayland Compositor.
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
package org.westford.compositor.core;

import org.freedesktop.wayland.server.WlSurfaceRequests;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.westford.compositor.protocol.WlSurface;

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
    private final LinkedList<SurfaceView>                   surfacesStack          = new LinkedList<>();
    @Nonnull
    private final Map<SurfaceView, LinkedList<SurfaceView>> subsurfaceStack        = new HashMap<>();
    @Nonnull
    private final Map<SurfaceView, LinkedList<SurfaceView>> pendingSubsurfaceStack = new HashMap<>();
    @Nonnull
    private final InfiniteRegion infiniteRegion;

    @Inject
    Scene(@Nonnull final InfiniteRegion infiniteRegion) {
        this.infiniteRegion = infiniteRegion;
    }

    public void removeSubsurfaceViewStack(@Nonnull final SurfaceView parentSurfaceView) {
        this.subsurfaceStack.remove(parentSurfaceView);
        this.pendingSubsurfaceStack.remove(parentSurfaceView);
    }

    public void commitSubsurfaceViewStack(@Nonnull final SurfaceView parentSurfaceView) {
        this.subsurfaceStack.put(parentSurfaceView,
                                 getPendingSubsurfaceViewStack(parentSurfaceView));
        this.pendingSubsurfaceStack.remove(parentSurfaceView);
    }

    /**
     * Get a pending z-ordered stack of subsurfaces grouped by their parent.
     * The returned subsurface stack is only valid until {@link #commitSubsurfaceViewStack(SurfaceView)} is called.
     *
     * @param parentSurfaceView the parent of the subsurfaces.
     *
     * @return A list of subsurfaces, including the parent, in z-order.
     */
    @Nonnull
    public LinkedList<SurfaceView> getPendingSubsurfaceViewStack(@Nonnull final SurfaceView parentSurfaceView) {
        //TODO unit test pending subsurface stack initialization
        return this.pendingSubsurfaceStack.computeIfAbsent(parentSurfaceView,
                                                           key -> new LinkedList<>(getSubsurfaceViewStack(parentSurfaceView)));
    }

    @Nonnull
    public LinkedList<SurfaceView> getSubsurfaceViewStack(@Nonnull final SurfaceView parentSurfaceView) {
        LinkedList<SurfaceView> subsurfaces = this.subsurfaceStack.get(parentSurfaceView);
        if (subsurfaces == null) {
            //TODO unit test subsurface stack initialization
            subsurfaces = new LinkedList<>();
            subsurfaces.add(parentSurfaceView);
            this.subsurfaceStack.put(parentSurfaceView,
                                     subsurfaces);
        }
        return subsurfaces;
    }

    @Nonnull
    public Optional<SurfaceView> pickSurfaceView(final Point global) {
        final Iterator<SurfaceView> surfaceIterator = getSurfacesStack().descendingIterator();
        Optional<SurfaceView>       pointerOver     = Optional.empty();
        while (surfaceIterator.hasNext()) {
            final SurfaceView       surfaceView       = surfaceIterator.next();
            final WlSurfaceResource wlSurfaceResource = surfaceView.getWlSurfaceResource();
            final WlSurfaceRequests implementation    = wlSurfaceResource.getImplementation();
            final Surface           surface           = ((WlSurface) implementation).getSurface();

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
                pointerOver = Optional.of(surfaceView);
                break;
            }
        }

        return pointerOver;
    }

    @Nonnull
    public LinkedList<SurfaceView> getSurfacesStack() {
        return this.surfacesStack;
    }
}
