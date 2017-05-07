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
package org.westford.compositor.wlshell

import com.google.auto.factory.AutoFactory
import com.google.auto.factory.Provided
import org.freedesktop.wayland.server.*
import org.freedesktop.wayland.shared.WlShellSurfaceResize
import org.freedesktop.wayland.shared.WlShellSurfaceTransient
import org.freedesktop.wayland.util.Fixed
import org.westford.compositor.core.*
import org.westford.compositor.core.calc.Mat4
import org.westford.compositor.core.events.KeyboardFocusGained
import org.westford.compositor.core.events.PointerGrab
import org.westford.compositor.protocol.WlKeyboard
import org.westford.compositor.protocol.WlPointer
import org.westford.compositor.protocol.WlSurface

@AutoFactory(className = "PrivateShellSurfaceFactory",
             allowSubclasses = true) class ShellSurface(@Provided display: Display,
                                                        @param:Provided private val compositor: Compositor,
                                                        @param:Provided private val scene: Scene,
                                                        private val surfaceView: SurfaceView,
                                                        private val pingSerial: Int) : Role {
    private val timerEventSource: EventSource

    private var keyboardFocusListener: ((KeyboardFocusGained) -> Unit)? = null

    var isActive = true
        private set

    var clazz: String? = null
        set(value) {
            field = value
            this.compositor.requestRender()
        }

    var title: String? = null
        set(value) {
            field = value
            this.compositor.requestRender()
        }

    init {
        this.timerEventSource = display.eventLoop.addTimer {
            this.isActive = false
            0
        }
    }

    fun pong(wlShellSurfaceResource: WlShellSurfaceResource,
             pingSerial: Int) {
        if (this.pingSerial == pingSerial) {
            this.isActive = true
            wlShellSurfaceResource.ping(pingSerial)
            this.timerEventSource.updateTimer(5000)
        }
    }

    fun move(wlSurfaceResource: WlSurfaceResource,
             wlPointerResource: WlPointerResource,
             grabSerial: Int) {

        val wlSurface = wlSurfaceResource.implementation as WlSurface
        val surface = wlSurface.surface

        val wlPointer = wlPointerResource.implementation as WlPointer
        val pointerDevice = wlPointer.pointerDevice

        val pointerPosition = pointerDevice.position

        pointerDevice.grab?.takeIf {
            surface.views.contains(it)
        }?.let {
            val surfacePosition = it.global(Point(0,
                                                  0))
            val pointerOffset = pointerPosition - surfacePosition

            //FIXME pick a surface view based on the pointer position
            pointerDevice.grabMotion(wlSurfaceResource,
                                     grabSerial) { motion ->
                it.updatePosition(motion.point - pointerOffset)
            }
        }
    }

    fun resize(wlShellSurfaceResource: WlShellSurfaceResource,
               wlSurfaceResource: WlSurfaceResource,
               wlPointerResource: WlPointerResource,
               buttonPressSerial: Int,
               edges: Int) {

        val wlSurface = wlSurfaceResource.implementation as WlSurface
        val surface = wlSurface.surface

        val wlPointer = wlPointerResource.implementation as WlPointer
        val pointerDevice = wlPointer.pointerDevice
        val pointerStartPos = pointerDevice.position

        pointerDevice.grab?.takeIf {
            surface.views.contains(it)
        }?.let {
            val local = it.local(pointerStartPos)
            val size = surface.size

            val quadrant = quadrant(edges)
            val transform = transform(quadrant,
                                      size,
                                      local)

            val inverseTransform = it.inverseTransform

            val grabMotionSuccess = pointerDevice.grabMotion(wlSurfaceResource,
                                                             buttonPressSerial) {
                val motionLocal = inverseTransform * it.point.toVec4()
                val resize = transform * motionLocal
                val width = resize.x.toInt()
                val height = resize.y.toInt()
                wlShellSurfaceResource.configure(quadrant.value,
                                                 if (width < 1) 1 else width,
                                                 if (height < 1) 1 else height)
            }

            if (grabMotionSuccess) {
                wlPointerResource.leave(pointerDevice.nextLeaveSerial(),
                                        wlSurfaceResource)
                pointerDevice.pointerGrabSignal.connect(object : (PointerGrab) -> Unit {
                    override fun invoke(event: PointerGrab) {
                        if (pointerDevice.grab == null) {
                            pointerDevice.pointerGrabSignal.disconnect(this)
                            wlPointerResource.enter(pointerDevice.nextEnterSerial(),
                                                    wlSurfaceResource,
                                                    Fixed.create(local.x),
                                                    Fixed.create(local.y))
                        }
                    }
                })
            }
        }
    }

    private fun quadrant(edges: Int): WlShellSurfaceResize {
        when (edges) {
            0    -> return WlShellSurfaceResize.NONE
            1    -> return WlShellSurfaceResize.TOP
            2    -> return WlShellSurfaceResize.BOTTOM
            4    -> return WlShellSurfaceResize.LEFT
            5    -> return WlShellSurfaceResize.TOP_LEFT
            6    -> return WlShellSurfaceResize.BOTTOM_LEFT
            8    -> return WlShellSurfaceResize.RIGHT
            9    -> return WlShellSurfaceResize.TOP_RIGHT
            10   -> return WlShellSurfaceResize.BOTTOM_RIGHT
            else -> return WlShellSurfaceResize.NONE
        }
    }

    private fun transform(quadrant: WlShellSurfaceResize,
                          size: Rectangle,
                          pointerLocal: Point): Mat4 {
        val width = size.width
        val height = size.height

        val transformation: Mat4
        val pointerdx: Float
        val pointerdy: Float
        when (quadrant) {
            WlShellSurfaceResize.TOP          -> {
                transformation = Transforms._180.copy(m00 = 0f,
                                                      m30 = width.toFloat())
                val pointerLocalTransformed = transformation * pointerLocal.toVec4()
                pointerdx = 0f
                pointerdy = height - pointerLocalTransformed.y
            }
            WlShellSurfaceResize.TOP_LEFT     -> {
                transformation = Transforms._180.copy(m30 = width.toFloat(),
                                                      m31 = height.toFloat())
                val localTransformed = transformation * pointerLocal.toVec4()
                pointerdx = width - localTransformed.x
                pointerdy = height - localTransformed.y
            }
            WlShellSurfaceResize.LEFT         -> {
                transformation = Transforms.FLIPPED.copy(m11 = 0f,
                                                         m31 = height.toFloat())
                val localTransformed = transformation * pointerLocal.toVec4()
                pointerdx = width - localTransformed.x
                pointerdy = 0f
            }
            WlShellSurfaceResize.BOTTOM_LEFT  -> {
                transformation = Transforms.FLIPPED.copy(m30 = width.toFloat())
                val localTransformed = transformation * pointerLocal.toVec4()
                pointerdx = width - localTransformed.x
                pointerdy = height - localTransformed.y
            }
            WlShellSurfaceResize.RIGHT        -> {
                transformation = Transforms.NORMAL.copy(m11 = 0f,
                                                        m31 = height.toFloat())
                val localTransformed = transformation * pointerLocal.toVec4()
                pointerdx = width - localTransformed.x
                pointerdy = 0f
            }
            WlShellSurfaceResize.TOP_RIGHT    -> {
                transformation = Transforms.FLIPPED_180.copy(m31 = height.toFloat())
                val localTransformed = transformation * pointerLocal.toVec4()
                pointerdx = width - localTransformed.x
                pointerdy = height - localTransformed.y
            }
            WlShellSurfaceResize.BOTTOM       -> {
                transformation = Transforms.NORMAL.copy(m00 = 0f,
                                                        m30 = width.toFloat())
                val pointerLocalTransformed = transformation * pointerLocal.toVec4()
                pointerdx = 0f
                pointerdy = height - pointerLocalTransformed.y
            }
            WlShellSurfaceResize.BOTTOM_RIGHT -> {
                transformation = Transforms.NORMAL
                val localTransformed = pointerLocal.toVec4()
                pointerdx = width - localTransformed.x
                pointerdy = height - localTransformed.y
            }
            else                              -> {
                transformation = Transforms.NORMAL
                pointerdx = 0f
                pointerdy = 0f
            }
        }

        return transformation.copy(m30 = (transformation.m30 + pointerdx),
                                   m31 = (transformation.m31 + pointerdy))
    }

    fun setTransient(wlSurfaceResource: WlSurfaceResource,
                     parent: WlSurfaceResource,
                     x: Int,
                     y: Int,
                     flags: Int) {
        val wlSurface = wlSurfaceResource.implementation as WlSurface
        val surface = wlSurface.surface

        this.keyboardFocusListener?.let {
            surface.keyboardFocusGainedSignal.disconnect(it)
        }

        if ((flags and WlShellSurfaceTransient.INACTIVE.value) != 0) {
            val slot: (KeyboardFocusGained) -> Unit = {
                //clean collection of focuses, so they don't get notify of keyboard related events
                surface.keyboardFocuses.clear()
            }
            surface.keyboardFocusGainedSignal.connect(slot)

            //first time focus clearing, also send out leave events
            val keyboardFocuses = surface.keyboardFocuses
            keyboardFocuses.forEach {
                val wlKeyboard = it.implementation as WlKeyboard
                val keyboardDevice = wlKeyboard.keyboardDevice
                it.leave(keyboardDevice.nextKeyboardSerial(),
                         wlSurfaceResource)
            }
            keyboardFocuses.clear()

            this.keyboardFocusListener = slot
        }

        val parentWlSurface = parent.implementation as WlSurface
        val parentSurface = parentWlSurface.surface

        this.scene.removeView(this.surfaceView)
        parentSurface.views.forEach {
            this.surfaceView.parent = it
        }

        parentSurface.addSibling(Sibling(Point(x,
                                               y),
                                         wlSurfaceResource))
    }

    override fun accept(roleVisitor: RoleVisitor) = roleVisitor.visit(this)

    fun setTopLevel(wlSurfaceResource: WlSurfaceResource) {
        val wlSurface = wlSurfaceResource.implementation as WlSurface
        val surface = wlSurface.surface

        surface.views.forEach {
            it.parent?.let {
                val parentWlSurfaceResource = it.wlSurfaceResource
                val parentWlSurface = parentWlSurfaceResource.implementation as WlSurface
                val parentSurface = parentWlSurface.surface
                parentSurface.removeSibling(Sibling(wlSurfaceResource))
            }
        }

        this.scene.removeView(this.surfaceView)
        this.scene.applicationLayer.surfaceViews.add(this.surfaceView)
    }
}
