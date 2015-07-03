package org.westmalle.wayland.core;

import org.freedesktop.wayland.server.WlSurfaceResource;

public interface Role {
    void beforeCommit(WlSurfaceResource wlSurfaceResource);
}
