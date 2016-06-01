package org.westmalle.wayland.core;

import org.freedesktop.wayland.server.WlSurfaceResource;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

@Singleton
public class Scene {
    @Nonnull
    private final LinkedList<WlSurfaceResource> surfacesStack = new LinkedList<>();
    @Nonnull
    private final Map<WlSurfaceResource, LinkedList<WlSurfaceResource>> subsurfaceStack = new HashMap<>();
    @Nonnull
    private final Map<WlSurfaceResource, LinkedList<WlSurfaceResource>> pendingSubsurfaceStack = new HashMap<>();

    @Inject
    Scene() {
    }

    @Nonnull
    public LinkedList<WlSurfaceResource> getSurfacesStack() {
        return this.surfacesStack;
    }

    public void removeSubsurfaceStack(@Nonnull final WlSurfaceResource parentSurface) {
        this.subsurfaceStack.remove(parentSurface);
        this.pendingSubsurfaceStack.remove(parentSurface);
    }

    /**
     * Get a pending z-ordered stack of subsurfaces grouped by their parent.
     * The returned subsurface stack is only valid until {@link #commitSubsurfaceStack(WlSurfaceResource)} is called.
     *
     * @param parentSurface the parent of the subsurfaces.
     * @return A list of subsurfaces, including the parent, in z-order.
     */
    @Nonnull
    public LinkedList<WlSurfaceResource> getPendingSubsurfaceStack(@Nonnull final WlSurfaceResource parentSurface) {
        LinkedList<WlSurfaceResource> subsurfaces = this.pendingSubsurfaceStack.get(parentSurface);
        if (subsurfaces == null) {
            //TODO unit test pending subsurface stack initialization
            subsurfaces = new LinkedList<>(getSubsurfaceStack(parentSurface));
            this.pendingSubsurfaceStack.put(parentSurface,
                                            subsurfaces);
        }
        return subsurfaces;
    }

    @Nonnull
    public LinkedList<WlSurfaceResource> getSubsurfaceStack(@Nonnull final WlSurfaceResource parentSurface) {
        LinkedList<WlSurfaceResource> subsurfaces = this.subsurfaceStack.get(parentSurface);
        if (subsurfaces == null) {
            //TODO unit test subsurface stack initialization
            subsurfaces = new LinkedList<>();
            subsurfaces.add(parentSurface);
            this.subsurfaceStack.put(parentSurface,
                                     subsurfaces);
        }
        return subsurfaces;
    }

    public void commitSubsurfaceStack(@Nonnull final WlSurfaceResource parentSurface) {
        this.subsurfaceStack.put(parentSurface,
                                 getPendingSubsurfaceStack(parentSurface));
        this.pendingSubsurfaceStack.remove(parentSurface);
    }
}
