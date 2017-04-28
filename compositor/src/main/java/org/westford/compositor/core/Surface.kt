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
import javax.annotation.Nonnegative

@AutoFactory(className = "SurfaceFactory",
             allowSubclasses = true) class Surface(@param:Provided private val finiteRegionFactory: FiniteRegionFactory,
                                                   @param:Provided private val compositor: Compositor,
                                                   @param:Provided private val renderer: Renderer,
                                                   @param:Provided private val surfaceViewFactory: SurfaceViewFactory) {

    val keyboardFocusLostSignal = Signal<KeyboardFocusLost>()
    val keyboardFocusGainedSignal = Signal<KeyboardFocusGained>()
    val applySurfaceStateSignal = Signal<SurfaceState>()
    val viewCreatedSignal = Signal<SurfaceView>()

    val frameCallbacks = mutableListOf<WlCallbackResource>()
    /**
     * The keyboards that will be used to notify the client of any keyboard events on this surface. This collection is
     * updated each time the keyboard focus changes for this surface. To keep the client from receiving keyboard events,
     * clear this list each time the focus is set for this surface. To listen for focus updates, register a keyboard focus
     * listener on this surface.
     * @return a set of keyboard resources.
     */
    val keyboardFocuses = mutableSetOf<WlKeyboardResource>();
    val pendingState: SurfaceState.Builder = SurfaceState.builder()
    val pendingSubsurfaces = mutableListOf<Subsurface>()
    /**
     * Return all sibling surfaces, including this surface.
     * @return
     */
    val siblings = mutableListOf<Sibling>()
    val views: MutableSet<SurfaceView>
        get() = this.surfaceViews

    var state = SurfaceState.builder().build()
    var role: Role? = null
    var renderState: SurfaceRenderState? = null

    /**
     * Surface level transformation. Contains transformations that should be applied on all views of this surface.
     * These are almost always scaling transformations. Positioning and rotation is done in [SurfaceView].
     * @return
     */
    var transform = Transforms.NORMAL
        private set
    var inverseTransform = Transforms.NORMAL
        private set
    var size = Rectangle.ZERO
        private set
    var isDestroyed: Boolean = false
        private set

    private val surfaceViews = mutableSetOf<SurfaceView>()

    private var pendingBufferDestroyListener: (() -> Unit)? = null

    fun markDestroyed() {
        this.isDestroyed = true
    }

    fun markDamaged(damage: Rectangle) {
        val damageRegion = this.pendingState.build().damage ?: this.finiteRegionFactory.create()
        damageRegion.add(damage)
        this.pendingState.damage(damageRegion)
    }

    fun attachBuffer(wlBufferResource: WlBufferResource,
                     dx: Int,
                     dy: Int) {
        pendingState.build().buffer?.unregister(this.pendingBufferDestroyListener)
        val detachBuffer = { this.detachBuffer() }
        wlBufferResource.register(detachBuffer)
        this.pendingBufferDestroyListener = detachBuffer
        pendingState.buffer(wlBufferResource).deltaPosition(Point.create(dx,
                                                                         dy))
    }

    fun commit() {
        val buffer = state.buffer
        //signal client that the previous buffer can be reused as we will now use the
        //newly attached buffer.
        buffer?.release()

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
        this.pendingSubsurfaces.forEach {
            this.siblings -= (it.sibling)
        }
        this.pendingSubsurfaces.asReversed().forEach {
            this.siblings += (it.sibling)
        }

        this.compositor.requestRender()

        applySurfaceStateSignal.emit(state)
    }

    fun detachBuffer() {
        pendingState.build().buffer?.unregister(this.pendingBufferDestroyListener)
        this.pendingBufferDestroyListener = null
        pendingState.buffer(null).damage(null)
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

        wlBufferResourceOptional?.let {
            val buffer = this.renderer.queryBuffer(it)
            val width = buffer.width / scale
            val height = buffer.height / scale

            this.size = Rectangle.builder().width(width).height(height).build()
        }
    }

    fun addCallback(callback: WlCallbackResource) = this.frameCallbacks.add(callback)

    fun removeOpaqueRegion() = this.pendingState.opaqueRegion(null)

    fun setOpaqueRegion(wlRegionResource: WlRegionResource) {
        val wlRegion = wlRegionResource.implementation as WlRegion
        val region = wlRegion.region
        this.pendingState.opaqueRegion(region)
    }

    fun removeInputRegion() = this.pendingState.inputRegion(null)

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

    fun setScale(@Nonnegative scale: Int) = pendingState.scale(scale)

    fun setBufferTransform(bufferTransform: Mat4) = pendingState.bufferTransform(bufferTransform)

    fun createView(wlSurfaceResource: WlSurfaceResource,
                   position: Point): SurfaceView {

        val surfaceView = this.surfaceViewFactory.create(wlSurfaceResource,
                                                         position)
        if (this.surfaceViews.add(surfaceView)) {
            siblings.forEach {
                ensureSiblingView(it,
                                  surfaceView)
            }
            pendingSubsurfaces.forEach {
                ensureSiblingView(it.sibling,
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

        siblingSurface.views.filter { it.parent == surfaceView }.forEach {
            //sibling already has a view with this surface as it's parent view. Do nothing.
            //TODO Perhaps we should we allow this?
            return
        }

        val siblingSurfaceView = siblingSurface.createView(siblingWlSurfaceResource,
                                                           surfaceView.global(siblingPosition))
        siblingSurfaceView.parent = surfaceView
        surfaceView.positionSignal.connect {
            siblingSurfaceView.setPosition(surfaceView.global(sibling.position))
        }
    }

    fun addSibling(sibling: Sibling) {
        views.forEach {
            ensureSiblingView(sibling,
                              it)
        }
        this.siblings.add(sibling)
        sibling.wlSurfaceResource.register {
            removeSibling(sibling)
        }
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
        views.forEach {
            ensureSiblingView(subsurfaceSibling,
                              it)
        }
        subsurfaceSibling.wlSurfaceResource.register {
            removeSubsurface(subsurface)
        }

        this.pendingSubsurfaces.add(subsurface)
    }

    fun removeSubsurface(subsurface: Subsurface) {
        if (this.pendingSubsurfaces.remove(subsurface)) {
            removeSibling(subsurface.sibling)
        }
    }
}