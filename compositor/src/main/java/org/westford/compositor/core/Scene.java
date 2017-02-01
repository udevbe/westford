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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Optional;

@Singleton
public class Scene {
    @Nonnull
    private final LinkedList<WlSurfaceResource> surfacesStack = new LinkedList<>();
    @Nonnull
    private final InfiniteRegion infiniteRegion;

    @Inject
    Scene(@Nonnull final InfiniteRegion infiniteRegion) {
        this.infiniteRegion = infiniteRegion;
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

            final Rectangle size = surface.getSize();

            for (SurfaceView surfaceView : surface.getViews()) {
                final Point local = surfaceView.local(global);
                if (region.contains(size,
                                    local)) {
                    pointerOver = Optional.of(surfaceResource);
                    break;
                }
            }
        }

        return pointerOver;
    }

    @Nonnull
    public LinkedList<WlSurfaceResource> getSurfacesStack() {
        return this.surfacesStack;
    }


    private LinkedList<SurfaceView> createSurfaceViewStack() {
        final LinkedList<SurfaceView> surfaceViews = new LinkedList<>();

        this.surfacesStack.forEach(wlSurfaceResource -> {
            final WlSurface wlSurface = (WlSurface) wlSurfaceResource.getImplementation();
            final Surface   surface   = wlSurface.getSurface();

            //siblings includes parent

            for (WlSurfaceResource siblingWlSurfaceResource : surface.getSiblings()) {

                final WlSurface siblingWlSurface = (WlSurface) siblingWlSurfaceResource.getImplementation();
                final Surface   siblingSurface   = siblingWlSurface.getSurface();

                for (SurfaceView siblingSurfaceView : siblingSurface.getViews()) {

                    //filter views based on this common parent
                    SurfaceView parentSurfaceView;
                    if (siblingSurface.equals(surface)) {
                        parentSurfaceView = siblingSurfaceView;
                    }
                    else if (siblingSurfaceView.getParent()
                                               .isPresent()) {
                        parentSurfaceView = siblingSurfaceView.getParent()
                                                              .get();
                    }
                    else {
                        //TODO what do we do here?
                        return;
                    }

                    surfaceViews.add(siblingSurfaceView);

                    //TODO iterate remaining
                    parentSurfaceView.getWlSurfaceResource();
                }
            }
        });

        return surfaceViews;
    }
}
