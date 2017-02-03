package org.westford.compositor.core;

import com.google.auto.value.AutoValue;
import org.freedesktop.wayland.server.WlSurfaceResource;

import javax.annotation.Nonnull;

@AutoValue
public abstract class Sibling {

    public static Sibling create(@Nonnull final WlSurfaceResource wlSurfaceResource,
                                 @Nonnull final Point position) {
        final Sibling sibling = new AutoValue_Sibling(wlSurfaceResource);
        sibling.setPosition(position);
        return sibling;
    }

    public static Sibling create(@Nonnull final WlSurfaceResource wlSurfaceResource) {
        return new AutoValue_Sibling(wlSurfaceResource);
    }

    @Nonnull
    private Point position = Point.ZERO;

    @Nonnull
    public abstract WlSurfaceResource getWlSurfaceResource();

    @Nonnull
    public Point getPosition() {
        return this.position;
    }

    public void setPosition(@Nonnull final Point position) {
        this.position = position;
    }

    @Override
    public String toString() {
        return "Sibling{"
               + "wlSurfaceResource=" + getWlSurfaceResource() + ","
               + "position=" + getPosition()
               + "}";
    }
}
