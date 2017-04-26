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

import org.freedesktop.wayland.server.Client
import org.freedesktop.wayland.server.Display
import org.freedesktop.wayland.server.WlTouchResource
import org.freedesktop.wayland.util.Fixed
import org.westford.Signal
import org.westford.Slot
import org.westford.compositor.core.events.TouchDown
import org.westford.compositor.core.events.TouchGrab
import org.westford.compositor.core.events.TouchMotion
import org.westford.compositor.core.events.TouchUp

import javax.annotation.Nonnegative
import javax.inject.Inject
import java.util.Optional
import java.util.stream.Collectors

class TouchDevice @Inject
internal constructor(private val display: Display,
                     private val scene: Scene) {

    val touchDownSignal = Signal<TouchDown, Slot<TouchDown>>()
    val touchGrabSignal = Signal<TouchGrab, Slot<TouchGrab>>()
    val touchMotionSignal = Signal<TouchMotion, Slot<TouchMotion>>()
    val touchUpSignal = Signal<TouchUp, Slot<TouchUp>>()

    var grab = Optional.empty<SurfaceView>()
        private set
    @Nonnegative
    var touchCount: Int = 0
        private set

    var downSerial: Int = 0
        private set
    var upSerial: Int = 0
        private set

    //TODO unit test
    fun cancel(wlTouchResources: Set<WlTouchResource>) {
        grab.ifPresent { surfaceView ->
            filter(wlTouchResources,
                    surfaceView.wlSurfaceResource
                            .client).forEach(Consumer<WlTouchResource> { it.cancel() })
        }
        this.grab = Optional.empty<SurfaceView>()
        this.touchCount = 0

        //TODO send event(s)?
    }

    private fun filter(wlTouchResources: Set<WlTouchResource>,
                       client: Client): Set<WlTouchResource> {
        //filter out touch resources that do not belong to the given client.
        return wlTouchResources.stream()
                .filter { wlPointerResource -> wlPointerResource.client == client }
                .collect<Set<WlTouchResource>, Any>(Collectors.toSet<WlTouchResource>())
    }

    //TODO unit test
    fun frame(wlTouchResources: Set<WlTouchResource>) {
        grab.ifPresent { surfaceView ->
            filter(wlTouchResources,
                    surfaceView.wlSurfaceResource
                            .client).forEach { wlTouchResource ->
                if (this.touchCount == 0) {
                    this.grab = Optional.empty<SurfaceView>()
                    this.touchGrabSignal.emit(TouchGrab.create())
                }
                wlTouchResource.frame()
            }
        }

        //TODO send event(s)?
    }

    //TODO unit test
    fun down(wlTouchResources: Set<WlTouchResource>,
             id: Int,
             time: Int,
             x: Int,
             y: Int) {

        //get a grabbed surface or try to establish new grab
        if (!grab.isPresent) {
            this.grab = this.scene.pickSurfaceView(Point.create(x,
                    y))
            this.touchGrabSignal.emit(TouchGrab.create())
        }

        //report 'down' to grab (if any)
        grab.ifPresent { surfaceView ->
            this.touchCount++

            val local = surfaceView.local(Point.create(x,
                    y))
            filter(wlTouchResources,
                    surfaceView.wlSurfaceResource
                            .client).forEach { wlTouchResource ->
                wlTouchResource.down(nextDownSerial(),
                        time,
                        surfaceView.wlSurfaceResource,
                        id,
                        Fixed.create(local.x),
                        Fixed.create(local.y))
            }
        }

        this.touchDownSignal.emit(TouchDown.create())
    }

    private fun nextDownSerial(): Int {
        this.downSerial = this.display.nextSerial()
        return this.downSerial
    }

    //TODO unit test
    fun up(wlTouchResources: Set<WlTouchResource>,
           id: Int,
           time: Int) {
        grab.ifPresent { surfaceView ->
            filter(wlTouchResources,
                    surfaceView.wlSurfaceResource
                            .client).forEach { wlTouchResource ->
                wlTouchResource.up(nextUpSerial(),
                        time,
                        id)
            }
            if (--this.touchCount < 0) {
                //safeguard against strange negative touch count (shouldn't happen normally)
                this.touchCount = 0
            }
        }

        this.touchUpSignal.emit(TouchUp.create())
    }

    private fun nextUpSerial(): Int {
        this.upSerial = this.display.nextSerial()
        return this.upSerial
    }

    //TODO unit test
    fun motion(wlTouchResources: Set<WlTouchResource>,
               id: Int,
               time: Int,
               x: Int,
               y: Int) {
        grab.ifPresent { surfaceView ->
            val local = surfaceView.local(Point.create(x,
                    y))
            filter(wlTouchResources,
                    surfaceView.wlSurfaceResource
                            .client).forEach { wlTouchResource ->
                wlTouchResource.motion(time,
                        id,
                        Fixed.create(local.x),
                        Fixed.create(local.y))
            }
        }

        this.touchMotionSignal.emit(TouchMotion.create())
    }
}
