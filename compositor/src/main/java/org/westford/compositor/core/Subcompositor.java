package org.westford.compositor.core;

import org.freedesktop.wayland.server.WlSurfaceResource;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.HashSet;
import java.util.LinkedList;

public class Subcompositor {

    @Nonnull
    private LinkedList<SurfaceView> pendingSurfacesStack = new LinkedList<>();

    @Nonnull
    private HashSet<WlSurfaceResource> subsurfaces = new HashSet<>();

    @Nonnull
    private final Scene scene;

    @Inject
    Subcompositor(@Nonnull final Scene scene) {
        this.scene = scene;
    }

    private void add(@Nonnull WlSurfaceResource wlSurfaceResource) {

        subsurfaces.add(wlSurfaceResource);
    }

    private void remove(@Nonnull WlSurfaceResource wlSurfaceResource) {
        subsurfaces.remove(wlSurfaceResource);
    }

    private boolean contains(@Nonnull WlSurfaceResource wlSurfaceResource) {
        return this.subsurfaces.contains(wlSurfaceResource);
    }
}
