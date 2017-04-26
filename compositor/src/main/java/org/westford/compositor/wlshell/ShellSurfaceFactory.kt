package org.westford.compositor.wlshell


import org.freedesktop.wayland.server.WlSurfaceResource
import org.westford.compositor.core.Point
import org.westford.compositor.core.Surface
import org.westford.compositor.core.SurfaceView
import javax.inject.Inject

class ShellSurfaceFactory @Inject
internal constructor(private val privateShellSurfaceFactory: PrivateShellSurfaceFactory) {

    fun create(wlSurfaceResource: WlSurfaceResource,
               surface: Surface,
               pingSerial: Int): ShellSurface {
        val view = surface.createView(wlSurfaceResource,
                Point.ZERO)
        return this.privateShellSurfaceFactory.create(view,
                pingSerial)
    }
}
