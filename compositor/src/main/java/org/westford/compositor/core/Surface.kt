/*
 * Westford Wayland Compositor.
 * Copyright (C) 2016  Erik De Rijcke
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.westford.compositor.core

import com.google.auto.factory.AutoFactory
import com.google.auto.factory.Provided
import org.freedesktop.wayland.server.*
import org.westford.Signal
import org.westford.compositor.core.calc.Mat4
import org.westford.compositor.core.events.KeyboardFocusGained
import org.westford.compositor.core.events.KeyboardFocusLost
import org.westford.compositor.protocol.WlRegion
import org.westford.compositor.protocol.WlSurface
import java.util.*
import javax.annotation.Nonnegative

@AutoFactory(className = "SurfaceFactory",
             allowSubclasses = true) class Surfaceconstructor(@param:Provided private val finiteRegionFactory: FiniteRegionFactory,
                                                              @param:Provided private val compositor: Compositor,
                                                              @param:Provided private val renderer: Renderer,
                                                              @param:Provided private val surfaceViewFactory: SurfaceViewFactory) {
    /*
     * Overall state
     */
    private val surfaceViews = HashSet<SurfaceView>()
    var role = Optional.empty<Role>(); private set

    /*
     * Signals
     */
    val keyboardFocusLostSignal = Signal<KeyboardFocusLost>()
    val keyboardFocusGainedSignal = Signal<KeyboardFocusGained>()
    val applySurfaceStateSignal = Signal<SurfaceState>()
    val viewCreatedSignal = Signal<SurfaceView>()
    val frameCallbacks: MutableList<WlCallbackResource> = LinkedList()
    /**
     * The keyboards that will be used to notify the client of any keyboard events on this surface. This collection is
     * updated each time the keyboard focus changes for this surface. To keep the client from receiving keyboard events,
     * clear this list each time the focus is set for this surface. To listen for focus updates, register a keyboard focus
     * listener on this surface.

     * @return a set of keyboard resources.
     */
    val keyboardFocuses: Set<WlKeyboardResource> = HashSet()

    /*
     * pending state
     */
    val pendingState: SurfaceState.Builder = SurfaceState.builder()
    private var pendingBufferDestroyListener = Optional.empty<DestroyListener>()
    val pendingSubsurfaces = LinkedList<Subsurface>()

    /*
     * committed state
     */
    var state = SurfaceState.builder().build()
    /**
     * Return all sibling surfaces, including this surface.

     * @return
     */
    val siblings = LinkedList<Sibling>()

    /*
     * committed derived states
     */
    var isDestroyed: Boolean = false; private set
    /**
     * Surface level transformation. Contains transformations that should be applied on all views of this surface.
     * These are almost always scaling transformations. Positioning and rotation is done in [SurfaceView].

     * @return
     */
    var transform = Transforms.NORMAL; private set
    var inverseTransform = Transforms.NORMAL; private set
    var size = Rectangle.ZERO; private set

    /*
     * render state
     */
    var renderState = Optional.empty<SurfaceRenderState>()
        private set

    fun markDestroyed() {
        this.isDestroyed = true
    }

    fun markDamaged(damage: Rectangle) {

        val damageRegion = this.pendingState.build().damage.orElseGet({ this.finiteRegionFactory.create() })
        damageRegion.add(damage)
        this.pendingState.damage(Optional.of(damageRegion))

    }

    fun attachBuffer(wlBufferResource: WlBufferResource,
                     dx: Int,
                     dy: Int) {
        pendingState.build().buffer.ifPresent { previousWlBufferResource -> previousWlBufferResource.unregister(this.pendingBufferDestroyListener.get()) }
        val detachBuffer = DestroyListener { this.detachBuffer() }
        wlBufferResource.register(detachBuffer)
        this.pendingBufferDestroyListener = Optional.of(detachBuffer)
        pendingState.buffer(Optional.of(wlBufferResource)).deltaPosition(Point.create(dx,
                                                                                      dy))
    }

    fun setRole(role: Role) {
        this.role = Optional.of(role)
    }

    fun commit() {
        val buffer = state.buffer
        //signal client that the previous buffer can be reused as we will now use the
        //newly attached buffer.
        buffer.ifPresent({ it.release() })

        //flush states
        apply(this.pendingState.build())

        //reset pending buffer state
        detachBuffer()
    }

    fun apply(surfaceState: SurfaceState) {
        state = surfaceState
        updateTransform()
        updateSize()

        //copy subsurface stack to siblings list. subsurfaces always go first in the sibling list.
        this.pendingSubsurfaces.forEach { subsurface -> this.siblings.remove(subsurface.sibling) }
        this.pendingSubsurfaces.descendingIterator().forEachRemaining { subsurface -> this.siblings.addFirst(subsurface.sibling) }

        this.compositor.requestRender()

        applySurfaceStateSignal.emit(state)
    }

    fun detachBuffer(): Surface {
        pendingState.build().buffer.ifPresent { wlBufferResource -> wlBufferResource.unregister(this.pendingBufferDestroyListener.get()) }
        this.pendingBufferDestroyListener = Optional.empty<DestroyListener>()
        pendingState.buffer(Optional.empty<WlBufferResource>()).damage(Optional.empty<Region>())
        return this
    }

    fun updateTransform() {
        val state = state
        this.transform = Transforms.SCALE(state.scale.toFloat()).multiply(state.bufferTransform)
        this.inverseTransform = transform.invert()
    }

    fun updateSize() {
        val state = state
        val wlBufferResourceOptional = state.buffer
        val scale = state.scale

        this.size = Rectangle.ZERO

        wlBufferResourceOptional.ifPresent { wlBufferResource ->
            val buffer = this.renderer.queryBuffer(wlBufferResource)
            val width = buffer.width / scale
            val height = buffer.height / scale

            this.size = Rectangle.builder().width(width).height(height).build()
        }
    }

    fun addCallback(callback: WlCallbackResource) {
        this.frameCallbacks.add(callback)
    }

    fun removeOpaqueRegion() {
        this.pendingState.opaqueRegion(Optional.empty<Region>())
    }

    fun setOpaqueRegion(wlRegionResource: WlRegionResource) {
        val wlRegion = wlRegionResource.implementation as WlRegion
        val region = wlRegion.region
        this.pendingState.opaqueRegion(Optional.of(region))
    }

    fun removeInputRegion() {
        this.pendingState.inputRegion(null)
    }

    fun setInputRegion(wlRegionResource: WlRegionResource) {
        val wlRegion = wlRegionResource.implementation as WlRegion
        val region = wlRegion.region
        pendingState.inputRegion(region)
    }

    fun firePaintCallbacks(serial: Int) {
        val callbacks = ArrayList(frameCallbacks)
        frameCallbacks.clear()
        callbacks.forEach {
            it.done(serial)
            it.destroy()
        }
    }

    fun setScale(@Nonnegative scale: Int) {
        pendingState.scale(scale)
    }

    fun setBufferTransform(bufferTransform: Mat4) {
        pendingState.bufferTransform(bufferTransform)
    }

    fun setRenderState(renderState: SurfaceRenderState) {
        this.renderState = Optional.of(renderState)
    }

    val views: Collection<SurfaceView>
        get() = Collections.unmodifiableCollection(this.surfaceViews)

    fun createView(wlSurfaceResource: WlSurfaceResource,
                   position: Point): SurfaceView {

        val surfaceView = this.surfaceViewFactory.create(wlSurfaceResource,
                                                         position)
        if (this.surfaceViews.add(surfaceView)) {
            siblings.forEach { sibling ->
                ensureSiblingView(sibling,
                                  surfaceView)
            }
            pendingSubsurfaces.forEach { subsurface ->
                ensureSiblingView(subsurface.sibling,
                                  surfaceView)
            }
            this.viewCreatedSignal.emit(surfaceView)
        }
        return surfaceView
    }

    private fun ensureSiblingView(sibling: Sibling,
                                  surfaceView: SurfaceView) {

        val siblingPosition = sibling.position
        val siblingWlSurfaceResource = sibling.wlSurfaceResource
        val siblingWlSurface = siblingWlSurfaceResource.implementation as WlSurface
        val siblingSurface = siblingWlSurface.surface

        if (siblingSurface == this) {
            return
        }

        for (siblingSurfaceView in siblingSurface.views) {
            val siblingSurfaceViewParent = siblingSurfaceView.parent
            if (siblingSurfaceViewParent != null && siblingSurfaceViewParent.get() == surfaceView) {
                //sibling already has a view with this surface as it's parent view. Do nothing.
                //TODO Perhaps we should we allow this?
                return
            }
        }

        val siblingSurfaceView = siblingSurface.createView(siblingWlSurfaceResource,
                                                           surfaceView.global(siblingPosition))
        siblingSurfaceView.setParent(surfaceView)
        surfaceView.positionSignal.connect({ event -> siblingSurfaceView.setPosition(surfaceView.global(sibling.position)) })
    }

    fun addSibling(sibling: Sibling) {
        views.forEach { surfaceView ->
            ensureSiblingView(sibling,
                              surfaceView)
        }
        this.siblings.add(sibling)
        sibling.wlSurfaceResource.register { removeSibling(sibling) }
    }

    fun removeSibling(sibling: Sibling) {
        if (this.siblings.remove(sibling)) {

            val siblingWlSurfaceResource = sibling.wlSurfaceResource
            val siblingWlSurface = siblingWlSurfaceResource.implementation as WlSurface

            siblingWlSurface.surface.views.forEach { it.removeParent() }
        }
    }

    fun addSubsurface(subsurface: Subsurface) {
        val subsurfaceSibling = subsurface.sibling
        views.forEach { surfaceView ->
            ensureSiblingView(subsurfaceSibling,
                              surfaceView)
        }
        subsurfaceSibling.wlSurfaceResource.register { removeSubsurface(subsurface) }

        this.pendingSubsurfaces.add(subsurface)
    }

    fun removeSubsurface(subsurface: Subsurface) {
        if (this.pendingSubsurfaces.remove(subsurface)) {
            removeSibling(subsurface.sibling)
        }
    }
}