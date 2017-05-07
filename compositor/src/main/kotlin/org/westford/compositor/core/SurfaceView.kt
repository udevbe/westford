package org.westford.compositor.core

import com.google.auto.factory.AutoFactory
import com.google.auto.factory.Provided
import org.freedesktop.wayland.server.WlSurfaceResource
import org.westford.Signal
import org.westford.compositor.core.calc.Mat4
import org.westford.compositor.protocol.WlSurface

@AutoFactory(allowSubclasses = true,
             className = "PrivateSurfaceViewFactory") class SurfaceView(@param:Provided private val compositor: Compositor,
                                                                        val wlSurfaceResource: WlSurfaceResource,
                                                                        positionTransform: Mat4,
                                                                        transform: Mat4,
                                                                        inverseTransform: Mat4) {
    val destroyedSignal = Signal<SurfaceView>()
    val positionSignal = Signal<Point>()

    var parent: SurfaceView? = null
        set(value) {
            field?.destroyedSignal?.disconnect(this::destroyOnParent)
            value?.destroyedSignal?.connect(this::destroyOnParent)
            field = value
        }

    var positionTransform: Mat4
        private set
    /**
     * Contains all view specific transformations, this includes positioning, rotation etc. of the view.
     *
     *
     * Translates from view local coordinates to compositor global coordinates.

     * @return
     */
    var transform: Mat4
        private set
    /**
     * Inverse of {[.getTransform]}. Translates from compositor global coordinates to view local coordinates.

     * @return
     */
    var inverseTransform: Mat4
        private set

    /**
     * Indicates if this view should be rendered.

     * @return
     */
    var isEnabled = true
    /**
     * Indicates if this view is drawable ie. does it have a buffer that can be rendered.

     * @return
     */
    var isDrawable = false
        private set

    init {
        this.positionTransform = positionTransform
        this.transform = transform
        this.inverseTransform = inverseTransform
    }

    private fun setPosition(positionTransform: Mat4) {
        this.positionTransform = positionTransform

        val wlSurface = wlSurfaceResource.implementation as WlSurface
        val surface = wlSurface.surface
        val surfaceTransform = surface.transform

        this.transform = this.positionTransform * surfaceTransform
        this.inverseTransform = this.transform.invert()
    }

    fun updatePosition(global: Point) {
        setPosition(Transforms.TRANSLATE(global.x,
                                         global.y))
        positionSignal.emit(global)

        this.compositor.requestRender()
    }

    fun onApply(surfaceState: SurfaceState) {

        this.isDrawable = surfaceState.buffer != null

        val deltaPosition = surfaceState.deltaPosition
        val dx = deltaPosition.x
        val dy = deltaPosition.y

        setPosition(this.positionTransform * Transforms.TRANSLATE(dx,
                                                                  dy))
    }

    private fun destroyOnParent(parent: SurfaceView) {
        destroy()
    }

    fun destroy() {
        val wlSurface = this.wlSurfaceResource.implementation as WlSurface
        val surface = wlSurface.surface
        surface.views -= this

        this.destroyedSignal.emit(this)
        this.parent = null
    }

    /**
     * Conveniently translate from a compositor global coordinate to a view local coordinate.

     * @param global A point from the compositor global plane.
     * *
     * *
     * @return A point in view local plane.
     */
    fun local(global: Point): Point {
        val localPoint = this.inverseTransform * global.toVec4()
        return Point(localPoint.x.toInt(),
                     localPoint.y.toInt())
    }

    /**
     * Conveniently translate from a view local coordinate to a compositor global coordinate.

     * @param surfaceLocal A point from the view local plane.
     * *
     * *
     * @return A point in the compositor global plane.
     */
    fun global(surfaceLocal: Point): Point {
        val globalPoint = this.transform * surfaceLocal.toVec4()
        return Point(globalPoint.x.toInt(),
                     globalPoint.y.toInt())
    }
}
