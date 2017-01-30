package org.westford.compositor.core;

import com.google.auto.factory.AutoFactory;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.westford.Signal;
import org.westford.Slot;
import org.westford.compositor.core.calc.Mat4;

import javax.annotation.Nonnull;

@AutoFactory
public class SurfaceView {

    @Nonnull
    private final Signal<Point, Slot<Point>> positionSignal = new Signal<>();

    @Nonnull
    private final WlSurfaceResource wlSurfaceResource;
    @Nonnull
    private Mat4 positionTransform = Mat4.IDENTITY;

    SurfaceView(@Nonnull WlSurfaceResource wlSurfaceResource) {
        this.wlSurfaceResource = wlSurfaceResource;
    }

    @Nonnull
    public WlSurfaceResource getWlSurfaceResource() {
        return this.wlSurfaceResource;
    }

    @Nonnull
    public Mat4 getPositionTransform() {
        return this.positionTransform;
    }

    public void setPosition(@Nonnull final Point global) {
        this.positionTransform = Transforms.TRANSLATE(global.getX(),
                                                      global.getY());
        getPositionSignal().emit(global);
    }

    @Nonnull
    public Signal<Point, Slot<Point>> getPositionSignal() {
        return this.positionSignal;
    }

    public void onApply(@Nonnull final SurfaceState surfaceState) {
        final Point deltaPosition = surfaceState.getDeltaPosition();
        final int   dx            = deltaPosition.getX();
        final int   dy            = deltaPosition.getY();

        //TODO more
    }
}
