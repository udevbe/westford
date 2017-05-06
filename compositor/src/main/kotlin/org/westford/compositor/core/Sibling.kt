package org.westford.compositor.core

import org.freedesktop.wayland.server.WlSurfaceResource
import org.westford.compositor.protocol.WlSurface

data class Sibling(private val _position: Point,
                   val wlSurfaceResource: WlSurfaceResource) {

    constructor(wlSurfaceResource: WlSurfaceResource) : this(Point.ZERO,
                                                             wlSurfaceResource)

    var position = _position
        set(value) {
            field = value
            updateSurfaceViewsPosition()
        }

    /**
     * Update all views' position of this sibling surface, with respect to their parent view. This method should be
     * called if the parent view moved to ensure child views move with it.
     */
    fun updateSurfaceViewsPosition() {
        val wlSurface = wlSurfaceResource.implementation as WlSurface
        val surface = wlSurface.surface

        surface.views.forEach {
            it.parent?.let { parentSurfaceView ->
                it.setPosition(parentSurfaceView.global(this.position))
            }
        }
    }
}
