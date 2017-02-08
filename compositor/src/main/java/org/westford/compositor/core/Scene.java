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
    public Optional<SurfaceView> pickSurfaceView(final Point global) {

        final Iterator<SurfaceView> surfaceViewIterator = createSurfaceViewStack().descendingIterator();
        Optional<SurfaceView>       pointerOver         = Optional.empty();

        while (surfaceViewIterator.hasNext()) {
            final SurfaceView surfaceView = surfaceViewIterator.next();

            if (!surfaceView.isDrawable() || !surfaceView.isEnabled()) {
                continue;
            }

            final WlSurfaceResource surfaceResource = surfaceView.getWlSurfaceResource();
            final WlSurfaceRequests implementation  = surfaceResource.getImplementation();
            final Surface           surface         = ((WlSurface) implementation).getSurface();

            final Optional<Region> inputRegion = surface.getState()
                                                        .getInputRegion();
            final Region region = inputRegion.orElseGet(() -> this.infiniteRegion);

            final Rectangle size = surface.getSize();

            final Point local = surfaceView.local(global);
            if (region.contains(size,
                                local)) {
                pointerOver = Optional.of(surfaceView);
                break;
            }
        }

        return pointerOver;
    }

    @Nonnull
    public LinkedList<WlSurfaceResource> getSurfacesStack() {
        return this.surfacesStack;
    }


    public LinkedList<SurfaceView> createSurfaceViewStack() {

        final LinkedList<SurfaceView> surfaceViews = new LinkedList<>();
        this.surfacesStack.forEach(wlSurfaceResource -> loopSiblings(wlSurfaceResource,
                                                                     surfaceViews));

        return surfaceViews;
    }

    private void addSiblingViews(final SurfaceView parentSurfaceView,
                                 final LinkedList<SurfaceView> surfaceViews) {

        final WlSurfaceResource parentWlSurfaceResource = parentSurfaceView.getWlSurfaceResource();
        final WlSurface         parentWlSurface         = (WlSurface) parentWlSurfaceResource.getImplementation();
        final Surface           parentSurface           = parentWlSurface.getSurface();

        parentSurface.getSiblings()
                     .forEach(sibling -> {

                         final WlSurface siblingWlSurface = (WlSurface) sibling.getWlSurfaceResource()
                                                                               .getImplementation();
                         final Surface siblingSurface = siblingWlSurface.getSurface();

                         //only consider surface if it has a role.
                         //TODO we could move the views to the generic role itf.
                         if (siblingSurface.getRole()
                                           .isPresent()) {

                             siblingSurface.getViews()
                                           .forEach(siblingSurfaceView -> {

                                               if (siblingSurfaceView.getParent()
                                                                     .filter(siblingParentSurfaceView ->
                                                                                     siblingParentSurfaceView.equals(parentSurfaceView))
                                                                     .isPresent()) {
                                                   addSiblingViews(siblingSurfaceView,
                                                                   surfaceViews);
                                               }
                                               else if (siblingSurfaceView.equals(parentSurfaceView)) {
                                                   surfaceViews.addFirst(siblingSurfaceView);
                                               }
                                           });
                         }
                     });
    }

    private void loopSiblings(WlSurfaceResource wlSurfaceResource,
                              LinkedList<SurfaceView> surfaceViews) {

        final WlSurface wlSurface = (WlSurface) wlSurfaceResource.getImplementation();
        final Surface   surface   = wlSurface.getSurface();

        surface.getViews()
               .forEach(parentSurfaceView -> addSiblingViews(parentSurfaceView,
                                                             surfaceViews));
    }
}
