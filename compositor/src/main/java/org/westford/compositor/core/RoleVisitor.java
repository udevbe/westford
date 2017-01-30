package org.westford.compositor.core;


import org.westford.compositor.wlshell.ShellSurface;

public interface RoleVisitor {

    default void visit(Role role) {}

    default void visit(PointerDevice pointerDevice) {}

    default void visit(ShellSurface shellSurface) {}

    default void visit(Subsurface subsurface) {}
}
