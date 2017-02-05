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

import org.freedesktop.wayland.server.WlSurfaceResource;
import org.westford.compositor.protocol.WlSurface;

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
                                                                           Sibling.create(wlSurfaceResource),
                                                                           surface.getState());
        surface.getApplySurfaceStateSignal()
               .connect(subsurface::apply);

        final WlSurface parentWlSurface = (WlSurface) parentWlSurfaceResource.getImplementation();
        final Surface   parentSurface   = parentWlSurface.getSurface();

        parentSurface.getApplySurfaceStateSignal()
                     .connect((surfaceState) -> subsurface.onParentApply());

        parentSurface.getRole()
                     .ifPresent(role -> role.accept(new RoleVisitor() {
                         @Override
                         public void visit(final Subsurface parentSubsurface) {
                             parentSubsurface.getEffectiveSyncSignal()
                                             .connect(subsurface::updateEffectiveSync);
                         }
                     }));

        parentSurface.addSubsurface(subsurface);
        this.scene.getSurfacesStack()
                  .remove(wlSurfaceResource);

        /*
         * Docs says a subsurface with a destroyed parent must become inert.
         */
        parentWlSurfaceResource.register(subsurface::setInert);

        return subsurface;
    }
}
