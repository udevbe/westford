package org.westford.compositor.core;

import org.freedesktop.wayland.server.WlSurfaceResource;
import org.westford.compositor.core.calc.Mat4;
import org.westford.compositor.protocol.WlSurface;

import javax.annotation.Nonnull;
import javax.inject.Inject;

public class SurfaceViewFactory {

    @Nonnull
    private final PrivateSurfaceViewFactory privateSurfaceViewFactory;

    @Inject
    SurfaceViewFactory(@Nonnull final PrivateSurfaceViewFactory privateSurfaceViewFactory) {
        this.privateSurfaceViewFactory = privateSurfaceViewFactory;
    }

    SurfaceView create(@Nonnull WlSurfaceResource wlSurfaceResource,
                       @Nonnull Point globalPosition) {

        final WlSurface wlSurface        = (WlSurface) wlSurfaceResource.getImplementation();
        final Surface   surface          = wlSurface.getSurface();
        final Mat4      surfaceTransform = surface.getTransform();

        final Mat4 positionTransform = Transforms.TRANSLATE(globalPosition.getX(),
                                                            globalPosition.getY());

        Mat4 transform        = positionTransform.multiply(surfaceTransform);
        Mat4 inverseTransform = transform.invert();

        final SurfaceView surfaceView = this.privateSurfaceViewFactory.create(wlSurfaceResource,
                                                                              positionTransform,
                                                                              transform,
                                                                              inverseTransform);

        surface.getApplySurfaceStateSignal()
               .connect(surfaceView::onApply);

        return surfaceView;
    }
}
