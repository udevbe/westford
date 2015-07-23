package org.westmalle.wayland.core;

import org.freedesktop.wayland.server.WlSurfaceResource;

import javax.annotation.Nonnull;

public interface Role {
    default void beforeCommit(@Nonnull final WlSurfaceResource wlSurfaceResource) {}

    default void afterDestroy(@Nonnull final WlSurfaceResource wlSurfaceResource) {}
}
