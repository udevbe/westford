package org.westford.compositor.core;


import org.freedesktop.wayland.server.WlSurfaceResource;
import org.westford.compositor.protocol.WlSurface;

import javax.annotation.Nonnull;
import javax.inject.Inject;

public class SiblingSurfaceFactory {

    @Nonnull
    private final PrivateSiblingSurfaceFactory privateSiblingSurfaceFactor;

    @Inject
    SiblingSurfaceFactory(@Nonnull final PrivateSiblingSurfaceFactory privateSiblingSurfaceFactor) {
        this.privateSiblingSurfaceFactor = privateSiblingSurfaceFactor;
    }

    public SiblingSurface create(@Nonnull final WlSurfaceResource wlSurfaceResource,
                                 @Nonnull final Point position) {
        final WlSurface wlSurface = (WlSurface) wlSurfaceResource.getImplementation();

        return this.privateSiblingSurfaceFactor.create(wlSurfaceResource,
                                                       position);
    }
}
