package org.westford.compositor.core


import org.westford.compositor.wlshell.ShellSurface

interface RoleVisitor {

    fun visit(role: Role) {
        defaultAction(role)
    }

    fun visit(pointerDevice: PointerDevice) {
        defaultAction(pointerDevice)
    }

    fun visit(shellSurface: ShellSurface) {
        defaultAction(shellSurface)
    }

    open fun visit(subsurface: Subsurface) {
        defaultAction(subsurface)
    }

    open fun defaultAction(role: Role) {}
}
