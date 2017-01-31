package org.westford.compositor.core;

import org.freedesktop.wayland.server.WlSurfaceResource;
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
        final SurfaceView surfaceView = this.privateSurfaceViewFactory.create(wlSurfaceResource,
                                                                              Transforms.TRANSLATE(globalPosition.getX(),
                                                                                                   globalPosition.getY()));
        final WlSurface wlSurface = (WlSurface) wlSurfaceResource.getImplementation();
        final Surface   surface   = wlSurface.getSurface();

        surface.getApplySurfaceStateSignal()
               .connect(surfaceView::onApply);

        return surfaceView;
    }
}
