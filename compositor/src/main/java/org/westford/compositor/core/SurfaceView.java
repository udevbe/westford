package org.westford.compositor.core;

import com.google.auto.factory.AutoFactory;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.westford.Signal;
import org.westford.Slot;
import org.westford.compositor.core.calc.Mat4;

import javax.annotation.Nonnull;

@AutoFactory(allowSubclasses = true,
             className = "PrivateSurfaceViewFactory")
public class SurfaceView {

    @Nonnull
    private final Signal<SurfaceView, Slot<SurfaceView>> destroyedSignal = new Signal<>();
    @Nonnull
    private final Signal<Point, Slot<Point>>             positionSignal  = new Signal<>();

    @Nonnull
    private final WlSurfaceResource wlSurfaceResource;
    @Nonnull
    private       Mat4              positionTransform;

    SurfaceView(@Nonnull WlSurfaceResource wlSurfaceResource,
                @Nonnull Mat4 positionTransform) {
        this.wlSurfaceResource = wlSurfaceResource;
        this.positionTransform = positionTransform;
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

        this.positionTransform = this.positionTransform.multiply(Transforms.TRANSLATE(dx,
                                                                                      dy));
    }

    @Nonnull
    public Signal<SurfaceView, Slot<SurfaceView>> getDestroyedSignal() {
        return this.destroyedSignal;
    }

    public void destroy() {
        this.destroyedSignal.emit(this);
    }
}
