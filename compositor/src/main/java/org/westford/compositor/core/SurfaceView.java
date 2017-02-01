package org.westford.compositor.core;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.westford.Signal;
import org.westford.Slot;
import org.westford.compositor.core.calc.Mat4;
import org.westford.compositor.core.calc.Vec4;
import org.westford.compositor.protocol.WlSurface;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.Optional;

@AutoFactory(allowSubclasses = true,
             className = "PrivateSurfaceViewFactory")
public class SurfaceView {

    @Nonnull
    private final Signal<SurfaceView, Slot<SurfaceView>> destroyedSignal = new Signal<>();
    @Nonnull
    private final Signal<Point, Slot<Point>>             positionSignal  = new Signal<>();

    @Nonnull
    private Optional<SurfaceView> parent = Optional.empty();

    @Nonnull
    private final Scene             scene;
    @Nonnull
    private final WlSurfaceResource wlSurfaceResource;

    @Nonnull
    private Mat4 positionTransform;

    @Nonnull
    private Mat4 transform;
    @Nonnull
    private Mat4 inverseTransform;

    SurfaceView(@Provided @Nonnull Scene scene,
                @Nonnull WlSurfaceResource wlSurfaceResource,
                @Nonnull Mat4 positionTransform,
                @Nonnull Mat4 transform,
                @Nonnull Mat4 inverseTransform) {
        this.scene = scene;
        this.wlSurfaceResource = wlSurfaceResource;
        this.positionTransform = positionTransform;
        this.transform = transform;
        this.inverseTransform = inverseTransform;
    }

    @Nonnull
    public WlSurfaceResource getWlSurfaceResource() {
        return this.wlSurfaceResource;
    }

    @Nonnull
    public Mat4 getPositionTransform() {
        return this.positionTransform;
    }

    private void setPosition(final Mat4 positionTransform) {
        this.positionTransform = positionTransform;

        final WlSurface wlSurface        = (WlSurface) getWlSurfaceResource().getImplementation();
        final Surface   surface          = wlSurface.getSurface();
        final Mat4      surfaceTransform = surface.getTransform();

        this.transform = this.positionTransform.multiply(surfaceTransform);
        this.inverseTransform = this.transform.invert();
    }

    public void setPosition(@Nonnull final Point global) {
        setPosition(Transforms.TRANSLATE(global.getX(),
                                         global.getY()));
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

        setPosition(this.positionTransform.multiply(Transforms.TRANSLATE(dx,
                                                                         dy)));
    }

    @Nonnull
    public Signal<SurfaceView, Slot<SurfaceView>> getDestroyedSignal() {
        return this.destroyedSignal;
    }

    public void destroy() {
        final WlSurface wlSurface = (WlSurface) this.wlSurfaceResource.getImplementation();
        final Surface   surface   = wlSurface.getSurface();
        surface.getViews()
               .remove(this);

        this.destroyedSignal.emit(this);
        removeParent();
    }

    @Nonnull
    public Optional<SurfaceView> getParent() {
        return this.parent;
    }

    public void setParent(@Nonnull final SurfaceView parent) {
        removeParent();

        this.scene.getSurfacesStack()
                  .remove(this);
        this.parent = Optional.of(parent);
    }

    public void removeParent() {
        this.parent.ifPresent(parentSurfaceView -> this.parent = Optional.empty());
    }

    @Nonnull
    public Point local(@Nonnull final Point global) {
        final Vec4 localPoint = this.inverseTransform.multiply(global.toVec4());
        return Point.create((int) localPoint.getX(),
                            (int) localPoint.getY());
    }

    @Nonnull
    public Point global(@Nonnull final Point surfaceLocal) {
        final Vec4 globalPoint = this.transform.multiply(surfaceLocal.toVec4());
        return Point.create((int) globalPoint.getX(),
                            (int) globalPoint.getY());
    }
}
