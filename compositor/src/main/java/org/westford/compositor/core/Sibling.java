package org.westford.compositor.core;

import com.google.auto.value.AutoValue;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.westford.compositor.protocol.WlSurface;

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
        updateSurfaceViewsPosition();
    }

    public void updateSurfaceViewsPosition() {
        final WlSurface wlSurface = (WlSurface) getWlSurfaceResource().getImplementation();
        final Surface   surface   = wlSurface.getSurface();

        surface.getViews()
               .forEach(surfaceView ->
                                surfaceView.getParent()
                                           .ifPresent(parentSurfaceView ->
                                                              surfaceView.setPosition(parentSurfaceView.global(this.position))));
    }

    @Override
    public String toString() {
        return "Sibling{"
               + "wlSurfaceResource=" + getWlSurfaceResource() + ","
               + "position=" + getPosition()
               + "}";
    }
}
