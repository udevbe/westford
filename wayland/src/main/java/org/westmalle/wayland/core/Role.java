package org.westmalle.wayland.core;

import org.freedesktop.wayland.server.Resource;

public interface Role<T extends Resource<?>> {
    void assigned(T protocolRoleObject);
    void beforeCommit();
}
