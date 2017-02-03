package org.westford.compositor.core;

import com.google.auto.factory.AutoFactory;
import org.freedesktop.wayland.server.WlSurfaceResource;

import javax.annotation.Nonnull;

@AutoFactory(allowSubclasses = true,
             className = "PrivateSiblingSurfaceFactory")
public class SiblingSurface {
    @Nonnull
    private final WlSurfaceResource wlSurfaceResource;

    @Nonnull
    private Point position;

    SiblingSurface(@Nonnull WlSurfaceResource wlSurfaceResource,
                   @Nonnull Point position) {
        this.wlSurfaceResource = wlSurfaceResource;
        this.position = position;
    }

    @Nonnull
    public WlSurfaceResource getWlSurfaceResource() {
        return wlSurfaceResource;
    }

    @Nonnull
    public Point getPosition() {
        return position;
    }

    public void setPosition(@Nonnull final Point position) {
        this.position = position;
    }
}
