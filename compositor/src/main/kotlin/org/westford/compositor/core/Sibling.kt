package org.westford.compositor.core

import com.google.auto.value.AutoValue
import org.freedesktop.wayland.server.WlSurfaceResource
import org.westford.compositor.protocol.WlSurface

@AutoValue abstract class Sibling {

    var position = Point.ZERO
        set(position) {
            field = position
            updateSurfaceViewsPosition()
        }

    abstract val wlSurfaceResource: WlSurfaceResource

    /**
     * Update all views' position of this sibling surface, with respect to their parent view. This method should be
     * called if the parent view moved to ensure child views move with it.
     */
    fun updateSurfaceViewsPosition() {
        val wlSurface = wlSurfaceResource.implementation as WlSurface
        val surface = wlSurface.surface

        surface.views.forEach { surfaceView ->
            surfaceView.parent.ifPresent { parentSurfaceView -> surfaceView.setPosition(parentSurfaceView.global(this.position)) }
        }
    }

    override fun toString(): String {
        return "Sibling{"
        +"wlSurfaceResource=" + wlSurfaceResource + ","
        +"position=" + position
        +"}"
    }

    companion object {

        fun create(wlSurfaceResource: WlSurfaceResource,
                   position: Point): Sibling {
            val sibling = AutoValue_Sibling(wlSurfaceResource)
            sibling.position = position
            return sibling
        }

        fun create(wlSurfaceResource: WlSurfaceResource): Sibling {
            return AutoValue_Sibling(wlSurfaceResource)
        }
    }
}
