package org.westmalle.wayland.core;


import org.freedesktop.wayland.server.WlSurfaceResource;
import org.westmalle.wayland.core.events.Slot;
import org.westmalle.wayland.protocol.WlSurface;

import javax.annotation.Nonnull;

public class SubsurfaceFactory {

    public Subsurface create(@Nonnull final WlSurfaceResource parentWlSurfaceResource,
                             @Nonnull final WlSurfaceResource wlSurfaceResource) {

        final WlSurface parentWlSurface = (WlSurface) parentWlSurfaceResource.getImplementation();
        final Surface   parentSurface   = parentWlSurface.getSurface();
        final Point position = parentSurface.global(Point.create(0,
                                                                 0));
        //set sync mode
        final WlSurface          wlSurface        = (WlSurface) wlSurfaceResource.getImplementation();
        final Slot<SurfaceState> parentCommitSlot = event -> wlSurface.commit(wlSurfaceResource);
        parentSurface.getCommitSignal()
                     .connect(parentCommitSlot);

        return new Subsurface(parentWlSurfaceResource,
                              wlSurfaceResource,
                              parentCommitSlot,
                              position);
    }
}
