package org.westford.compositor.core;


import org.westford.compositor.wlshell.ShellSurface;

public interface RoleVisitor {

    default void visit(Role role) {defaultAction(role);}

    default void visit(PointerDevice pointerDevice) {defaultAction(pointerDevice);}

    default void visit(ShellSurface shellSurface) {defaultAction(shellSurface);}

    default void visit(Subsurface subsurface) {defaultAction(subsurface);}

    default void defaultAction(Role role) {}
}
