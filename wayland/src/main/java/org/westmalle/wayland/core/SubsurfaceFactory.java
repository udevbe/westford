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

import org.freedesktop.wayland.server.DestroyListener;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.westmalle.wayland.protocol.WlCompositor;
import org.westmalle.wayland.protocol.WlSurface;

import javax.annotation.Nonnull;
import javax.inject.Inject;

public class SubsurfaceFactory {

    @Nonnull
    private final PrivateSubsurfaceFactory privateSubsurfaceFactory;
    @Nonnull
    private final Scene                    scene;

    @Inject
    SubsurfaceFactory(@Nonnull final PrivateSubsurfaceFactory privateSubsurfaceFactory,
                      @Nonnull final Scene scene) {
        this.privateSubsurfaceFactory = privateSubsurfaceFactory;
        this.scene = scene;
    }

    public Subsurface create(@Nonnull final WlSurfaceResource parentWlSurfaceResource,
                             @Nonnull final WlSurfaceResource wlSurfaceResource) {

        final WlSurface wlSurface = (WlSurface) wlSurfaceResource.getImplementation();
        final Surface   surface   = wlSurface.getSurface();

        final Subsurface subsurface = this.privateSubsurfaceFactory.create(parentWlSurfaceResource,
                                                                           wlSurfaceResource,
                                                                           surface.getState());
        surface.getApplySurfaceStateSignal()
               .connect(subsurface::apply);

        final WlSurface parentWlSurface = (WlSurface) parentWlSurfaceResource.getImplementation();
        final Surface   parentSurface   = parentWlSurface.getSurface();

        parentSurface.getApplySurfaceStateSignal()
                     .connect((surfaceState) -> subsurface.onParentApply());
        parentSurface.getPositionSignal()
                     .connect(event -> subsurface.applyPosition());
        parentSurface.getRole()
                     .ifPresent(role -> {
                         if (role instanceof Subsurface) {
                             final Subsurface parentSubsurface = (Subsurface) role;
                             parentSubsurface.getEffectiveSyncSignal()
                                             .connect(subsurface::updateEffectiveSync);
                         }
                     });

        this.scene.getSurfacesStack()
                  .remove(wlSurfaceResource);
        this.scene.getSubsurfaceStack(parentWlSurfaceResource)
                  .addLast(wlSurfaceResource);

        final DestroyListener destroyListener = () -> {
            this.scene.getSubsurfaceStack(parentWlSurfaceResource)
                      .remove(wlSurfaceResource);
            this.scene.getPendingSubsurfaceStack(parentWlSurfaceResource)
                      .remove(wlSurfaceResource);
        };
        wlSurfaceResource.register(destroyListener);

        parentWlSurfaceResource.register(() -> {
            /*
             * A destroyed parent will have it's stack of subsurfaces removed, so no need to remove the subsurface
             * from that stack (which is done in the subsurface destroy listener).
             */
            wlSurfaceResource.unregister(destroyListener);
            /*
             * Docs says a subsurface with a destroyed parent must become inert.
             */
            subsurface.setInert(true);
        });

        return subsurface;
    }
}
