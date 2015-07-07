package org.westmalle.wayland.core;

import org.freedesktop.wayland.server.WlSurfaceResource;

public interface Role {
    default void beforeCommit(final WlSurfaceResource wlSurfaceResource) {}
}
