package org.westford.compositor.wlshell;


import org.freedesktop.wayland.server.WlSurfaceResource;
import org.westford.compositor.core.Point;
import org.westford.compositor.core.Surface;
import org.westford.compositor.core.SurfaceView;

import javax.annotation.Nonnull;
import javax.inject.Inject;

public class ShellSurfaceFactory {


    @Nonnull
    private final PrivateShellSurfaceFactory privateShellSurfaceFactory;

    @Inject
    ShellSurfaceFactory(@Nonnull final PrivateShellSurfaceFactory privateShellSurfaceFactory) {
        this.privateShellSurfaceFactory = privateShellSurfaceFactory;
    }

    public ShellSurface create(@Nonnull final WlSurfaceResource wlSurfaceResource,
                               @Nonnull final Surface surface,
                               int pingSerial) {
        final SurfaceView view = surface.createView(wlSurfaceResource,
                                                    Point.ZERO);
        return this.privateShellSurfaceFactory.create(view,
                                                      pingSerial);
    }
}
