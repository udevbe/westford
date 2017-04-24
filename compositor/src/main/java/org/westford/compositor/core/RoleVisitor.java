package org.westford.compositor.core;


import org.westford.compositor.wlshell.ShellSurface;

public interface RoleVisitor {

    default void visit(final Role role) {defaultAction(role);}

    default void visit(final PointerDevice pointerDevice) {defaultAction(pointerDevice);}

    default void visit(final ShellSurface shellSurface) {defaultAction(shellSurface);}

    default void visit(final Subsurface subsurface) {defaultAction(subsurface);}

    default void defaultAction(final Role role) {}
}
