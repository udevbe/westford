package org.westford.compositor.core

import com.google.auto.factory.AutoFactory
import com.google.auto.factory.Provided
import org.freedesktop.wayland.server.WlSurfaceResource
import org.westford.Signal
import org.westford.Slot
import org.westford.compositor.core.calc.Mat4
import org.westford.compositor.core.calc.Vec4
import org.westford.compositor.protocol.WlSurface
import java.util.Optional

@AutoFactory(allowSubclasses = true, className = "PrivateSurfaceViewFactory")
class SurfaceView internal constructor(@param:Provided private val compositor: Compositor,
                                       val wlSurfaceResource: WlSurfaceResource,
                                       positionTransform: Mat4,
                                       transform: Mat4,
                                       inverseTransform: Mat4) {

    val destroyedSignal = Signal<SurfaceView, Slot<SurfaceView>>()
    val positionSignal = Signal<Point, Slot<Point>>()

    var parent = Optional.empty<SurfaceView>()
        private set

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

        this.transform = this.positionTransform.multiply(surfaceTransform)
        this.inverseTransform = this.transform.invert()
    }

    fun setPosition(global: Point) {
        setPosition(Transforms.TRANSLATE(global.x,
                global.y))
        positionSignal.emit(global)

        this.compositor.requestRender()
    }

    fun onApply(surfaceState: SurfaceState) {

        this.isDrawable = surfaceState.buffer
                .isPresent

        val deltaPosition = surfaceState.deltaPosition
        val dx = deltaPosition.x
        val dy = deltaPosition.y

        setPosition(this.positionTransform.multiply(Transforms.TRANSLATE(dx,
                dy)))
    }

    fun destroy() {
        val wlSurface = this.wlSurfaceResource.implementation as WlSurface
        val surface = wlSurface.surface
        surface.views
                .remove(this)

        this.destroyedSignal.emit(this)
        removeParent()
    }

    fun setParent(parent: SurfaceView) {
        removeParent()
        parent.destroyedSignal
                .connect({ event -> destroy() })
        this.parent = Optional.of(parent)
    }

    fun removeParent() {
        this.parent.ifPresent { parentSurfaceView -> this.parent = Optional.empty<SurfaceView>() }
    }

    /**
     * Conveniently translate from a compositor global coordinate to a view local coordinate.

     * @param global A point from the compositor global plane.
     * *
     * *
     * @return A point in view local plane.
     */
    fun local(global: Point): Point {
        val localPoint = this.inverseTransform.multiply(global.toVec4())
        return Point.create(localPoint.x.toInt(),
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
        val globalPoint = this.transform.multiply(surfaceLocal.toVec4())
        return Point.create(globalPoint.x.toInt(),
                globalPoint.y.toInt())
    }
}
