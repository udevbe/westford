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
import org.freedesktop.wayland.shared.WlPointerAxis
import org.freedesktop.wayland.shared.WlPointerAxisSource
import org.freedesktop.wayland.shared.WlPointerButtonState
import org.freedesktop.wayland.util.Fixed
import org.westford.Signal
import org.westford.compositor.core.calc.Geo
import org.westford.compositor.core.events.Button
import org.westford.compositor.core.events.PointerFocus
import org.westford.compositor.core.events.PointerGrab
import org.westford.compositor.core.events.PointerMotion
import org.westford.compositor.protocol.WlSurface
import java.util.*
import java.util.stream.Collectors
import javax.annotation.Nonnegative
import javax.inject.Inject

@AutoFactory(allowSubclasses = true, className = "PrivatePointerDeviceFactory")
class PointerDevice @Inject
internal constructor(@param:Provided private val geo: Geo,
                     @param:Provided private val display: Display,
                     @param:Provided private val nullRegion: NullRegion,
                     @param:Provided private val cursorFactory: CursorFactory,
                     @param:Provided private val jobExecutor: JobExecutor,
                     @param:Provided private val scene: Scene,
                     val clampRegion: FiniteRegion) : Role {

    val motionSignal = Signal<PointerMotion, Slot<PointerMotion>>()
    val buttonSignal = Signal<Button, Slot<Button>>()
    val pointerGrabSignal = Signal<PointerGrab, Slot<PointerGrab>>()
    val pointerFocusSignal = Signal<PointerFocus, Slot<PointerFocus>>()

    private val pressedButtons = HashSet<Int>()
    private val cursors = HashMap<WlPointerResource, Cursor>()
    var position = Point.ZERO
        private set
    private var activeCursor = Optional.empty<Cursor>()
    private var grabDestroyListener = Optional.empty<DestroyListener>()
    var grab = Optional.empty<SurfaceView>()
        private set
    var focus = Optional.empty<SurfaceView>()
        private set
    private var focusDestroyListener = Optional.empty<DestroyListener>()
    var buttonPressSerial: Int = 0
        private set
    var buttonReleaseSerial: Int = 0
        private set
    var enterSerial: Int = 0
        private set
    var leaveSerial: Int = 0
        private set

    @Nonnegative
    private var buttonsPressed: Int = 0

    //TODO unit test
    fun axisSource(wlPointerResources: Set<WlPointerResource>,
                   wlPointerAxisSource: WlPointerAxisSource) {
        focus.ifPresent { surfaceView ->
            filter(wlPointerResources,
                    surfaceView.wlSurfaceResource
                            .client).forEach { wlPointerResource ->
                if (wlPointerResource.version > 4) {
                    wlPointerResource.axisSource(wlPointerAxisSource.value)
                }
            }
        }
        //TODO emit event?
    }

    private fun filter(wlPointerResources: Set<WlPointerResource>,
                       client: Client): Set<WlPointerResource> {
        //filter out pointer resources that do not belong to the given client.
        return wlPointerResources.stream()
                .filter { wlPointerResource -> wlPointerResource.client == client }
                .collect<Set<WlPointerResource>, Any>(Collectors.toSet<WlPointerResource>())
    }

    //TODO unit test
    fun frame(wlPointerResources: Set<WlPointerResource>) {
        focus.ifPresent { surfaceView ->
            filter(wlPointerResources,
                    surfaceView.wlSurfaceResource
                            .client).forEach { wlPointerResource ->
                if (wlPointerResource.version > 4) {
                    wlPointerResource.frame()
                }
            }
        }
        //TODO emit event?
    }

    //TODO unit test
    fun axisStop(wlPointerResources: Set<WlPointerResource>,
                 wlPointerAxis: WlPointerAxis,
                 time: Int) {
        focus.ifPresent { surfaceView ->
            reportAxisStop(wlPointerResources,
                    surfaceView,
                    wlPointerAxis,
                    time)
        }
        //TODO emit event?
    }

    private fun reportAxisStop(surfaceResource: Set<WlPointerResource>,
                               surfaceView: SurfaceView,
                               wlPointerAxis: WlPointerAxis,
                               time: Int) {
        filter(surfaceResource,
                surfaceView.wlSurfaceResource
                        .client).forEach { wlPointerResource ->
            if (wlPointerResource.version > 4) {
                wlPointerResource.axisStop(time,
                        wlPointerAxis.value)
            }
        }
    }

    //TODO unit test
    fun axisDiscrete(wlPointerResources: Set<WlPointerResource>,
                     wlPointerAxis: WlPointerAxis,
                     time: Int,
                     discrete: Int,
                     value: Float) {
        focus.ifPresent { surfaceView ->
            filter(wlPointerResources,
                    surfaceView.wlSurfaceResource
                            .client).forEach { wlPointerResource ->

                if (wlPointerResource.version > 4) {
                    wlPointerResource.axisDiscrete(wlPointerAxis.value,
                            discrete)
                }

                axisOrStop(wlPointerResource,
                        time,
                        wlPointerAxis,
                        value)
            }
        }
        //TODO emit event?
    }

    private fun axisOrStop(wlPointerResource: WlPointerResource,
                           time: Int,
                           wlPointerAxis: WlPointerAxis,
                           value: Float) {
        if (value != 0f) {
            wlPointerResource.axis(time,
                    wlPointerAxis.value,
                    Fixed.create(value))
        } else if (wlPointerResource.version > 4) {
            wlPointerResource.axisStop(time,
                    wlPointerAxis.value)
        }
    }

    //TODO unit test
    fun axisContinuous(wlPointerResources: Set<WlPointerResource>,
                       time: Int,
                       wlPointerAxis: WlPointerAxis,
                       value: Float) {
        focus.ifPresent { surfaceView ->
            filter(wlPointerResources,
                    surfaceView.wlSurfaceResource
                            .client).forEach { wlPointerResource ->
                axisOrStop(wlPointerResource,
                        time,
                        wlPointerAxis,
                        value)
            }
        }
        //TODO emit event?
    }

    /**
     * Move this pointer to a new absolute position and deliver a motion event to the client of the focused surface.

     * @param wlPointerResources a set of pointer resources that will be used to find the client.
     * *
     * @param x                  new absolute X
     * *
     * @param y                  new absolute Y
     */
    fun motion(wlPointerResources: Set<WlPointerResource>,
               time: Int,
               x: Int,
               y: Int) {

        clamp(wlPointerResources,
                Point.create(x,
                        y))

        focus.ifPresent { surfaceView ->
            reportMotion(wlPointerResources,
                    time,
                    surfaceView)
        }

        this.motionSignal.emit(PointerMotion.create(time,
                position))
    }

    fun calculateFocus(wlPointerResources: Set<WlPointerResource>) {
        val oldFocus = focus
        val newFocus = this.scene.pickSurfaceView(position)

        if (oldFocus != newFocus) {
            updateFocus(wlPointerResources,
                    oldFocus,
                    newFocus)
        }
    }

    private fun reportMotion(wlPointerResources: Set<WlPointerResource>,
                             time: Int,
                             surfaceView: SurfaceView) {

        val pointerPosition = position

        filter(wlPointerResources,
                surfaceView.wlSurfaceResource
                        .client).forEach { wlPointerResource ->
            val relativePoint = surfaceView.local(pointerPosition)
            wlPointerResource.motion(time,
                    Fixed.create(relativePoint.x),
                    Fixed.create(relativePoint.y))
        }

        this.activeCursor.ifPresent { cursor -> cursor.updatePosition(pointerPosition) }
    }

    private fun reportLeave(wlPointerResources: Set<WlPointerResource>,
                            surfaceView: SurfaceView) {
        wlPointerResources.forEach { wlPointerResource ->
            wlPointerResource.leave(nextLeaveSerial(),
                    surfaceView.wlSurfaceResource)
        }
    }

    private fun reportEnter(wlPointerResources: Set<WlPointerResource>,
                            surfaceView: SurfaceView) {
        wlPointerResources.forEach { wlPointerResource ->
            val relativePoint = surfaceView.local(position)
            wlPointerResource.enter(nextEnterSerial(),
                    surfaceView.wlSurfaceResource,
                    Fixed.create(relativePoint.x),
                    Fixed.create(relativePoint.y))
        }
    }

    fun nextLeaveSerial(): Int {
        this.leaveSerial = this.display.nextSerial()
        return leaveSerial
    }

    fun nextEnterSerial(): Int {
        this.enterSerial = this.display.nextSerial()
        return enterSerial
    }

    fun button(wlPointerResources: Set<WlPointerResource>,
               time: Int,
               @Nonnegative button: Int,
               wlPointerButtonState: WlPointerButtonState) {
        if (wlPointerButtonState == WlPointerButtonState.PRESSED) {
            this.pressedButtons.add(button)
        } else {
            this.pressedButtons.remove(button)
        }
        doButton(wlPointerResources,
                time,
                button,
                wlPointerButtonState)
        this.buttonSignal.emit(Button.create(time,
                button,
                wlPointerButtonState))
    }

    private fun doButton(wlPointerResources: Set<WlPointerResource>,
                         time: Int,
                         button: Int,
                         wlPointerButtonState: WlPointerButtonState) {

        if (wlPointerButtonState == WlPointerButtonState.PRESSED) {
            this.buttonsPressed++
        } else if (this.buttonsPressed > 0) {
            //make sure we only decrement if we had a least one increment.
            //Is such a thing even possible? Yes it is. (Aliens pressing a button before starting compositor)
            this.buttonsPressed--
        }

        val hasPress = this.buttonsPressed != 0
        val hasFocus = this.focus.isPresent
        val hasGrab = grab.isPresent

        if (hasPress &&
                !hasGrab &&
                hasFocus) {
            //no grab, but we do have a focus and a pressed button. Focused surface becomes grab.
            grab()
        }

        grab.ifPresent { surfaceView ->
            reportButton(wlPointerResources,
                    surfaceView,
                    time,
                    button,
                    wlPointerButtonState)

            if (!hasPress) {
                ungrab()
            }
        }
    }

    private fun grab() {
        this.grab = focus
        this.grabDestroyListener = Optional.of(DestroyListener { this.ungrab() })

        val surfaceView = grab.get()
        //if the surface having the grab is destroyed, we clear the grab
        surfaceView.wlSurfaceResource
                .register(this.grabDestroyListener.get())

        this.pointerGrabSignal.emit(PointerGrab.create(grab))
    }

    private fun reportButton(wlPointerResources: Set<WlPointerResource>,
                             surfaceView: SurfaceView,
                             time: Int,
                             button: Int,
                             wlPointerButtonState: WlPointerButtonState) {
        filter(wlPointerResources,
                surfaceView.wlSurfaceResource
                        .client).forEach { wlPointerResource ->
            wlPointerResource.button(if (wlPointerButtonState == WlPointerButtonState.PRESSED)
                nextButtonPressSerial()
            else
                nextButtonReleaseSerial(),
                    time,
                    button,
                    wlPointerButtonState.value)
        }
    }

    private fun ungrab() {
        //grab will be updated, don't listen for previous grab surface destruction.
        grab.ifPresent { surfaceView ->
            surfaceView.wlSurfaceResource
                    .unregister(this.grabDestroyListener.get())
        }
        this.grabDestroyListener = Optional.empty<DestroyListener>()
        this.grab = Optional.empty<SurfaceView>()
        this.pointerGrabSignal.emit(PointerGrab.create(grab))
    }

    fun nextButtonPressSerial(): Int {
        this.buttonPressSerial = this.display.nextSerial()
        return buttonPressSerial
    }

    fun nextButtonReleaseSerial(): Int {
        this.buttonReleaseSerial = this.display.nextSerial()
        return buttonReleaseSerial
    }

    private fun updateFocus(wlPointerResources: Set<WlPointerResource>,
                            oldFocus: Optional<SurfaceView>,
                            newFocus: Optional<SurfaceView>) {

        oldFocus.ifPresent { oldFocusView ->
            //remove old focus destroy listener
            oldFocusView.wlSurfaceResource
                    .unregister(this.focusDestroyListener.get())
            //notify client of focus lost
            reportLeave(filter(wlPointerResources,
                    oldFocusView.wlSurfaceResource
                            .client),
                    oldFocusView)
        }
        //clear ref to old destroy listener
        this.focusDestroyListener = Optional.empty<DestroyListener>()

        newFocus.ifPresent { newFocusView ->
            //if focus resource is destroyed, trigger schedule focus update. This guarantees that
            //the compositor removes and updates the list of active surfaces first.
            val destroyListener = { this.jobExecutor.submit { calculateFocus(wlPointerResources) } }
            this.focusDestroyListener = Optional.of<DestroyListener>(destroyListener)
            //add destroy listener
            newFocusView.wlSurfaceResource
                    .register(destroyListener)
            //notify client of new focus
            reportEnter(filter(wlPointerResources,
                    newFocusView.wlSurfaceResource
                            .client),
                    newFocusView)
        }

        //update focus to new focus
        this.focus = newFocus
        //notify listeners focus has changed
        this.pointerFocusSignal.emit(PointerFocus.create())
    }

    fun isButtonPressed(@Nonnegative button: Int): Boolean {
        return this.pressedButtons.contains(button)
    }

    /**
     * Listen for motion as soon as given surface is grabbed.
     *
     *
     * If another surface already has the grab, the listener
     * is never registered.

     * @param wlSurfaceResource Surface that is grabbed.
     * *
     * @param buttonPressSerial Serial that triggered the grab.
     * *
     * @param pointerGrabMotion PointerMotion listener.
     * *
     * *
     * @return true if the listener was registered, false if not.
     */
    fun grabMotion(wlSurfaceResource: WlSurfaceResource,
                   buttonPressSerial: Int,
                   pointerGrabMotion: PointerGrabMotion): Boolean {
        val wlSurface = wlSurfaceResource.implementation as WlSurface
        val surface = wlSurface.surface
        val surfaceViews = surface.views

        if (!grab.isPresent ||
                !surfaceViews.contains(grab.get()) ||
                buttonPressSerial != buttonPressSerial) {
            //preconditions not met
            return false
        }

        val motionSlot = object : Slot<PointerMotion> {

            override fun handle(motion: PointerMotion) {
                if (grab.isPresent && surfaceViews.contains(grab.get())) {
                    //there is pointer motion
                    pointerGrabMotion.motion(motion)
                } else {
                    //another surface has the grab, stop listening for pointer motion.
                    motionSignal.disconnect(this)
                }
            }
        }

        //listen for pointer motion
        motionSignal.connect(motionSlot)
        //listen for surface destruction
        wlSurfaceResource.register {
            //another surface has the grab, stop listening for pointer motion.
            motionSignal.disconnect(motionSlot)
        }

        return true
    }

    fun removeCursor(wlPointerResource: WlPointerResource,
                     serial: Int) {
        if (serial != enterSerial) {
            return
        }
        Optional.ofNullable(this.cursors.remove(wlPointerResource)).ifPresent { it.hide() }
    }

    fun setCursor(wlPointerResource: WlPointerResource,
                  serial: Int,
                  wlSurfaceResource: WlSurfaceResource,
                  hotspotX: Int,
                  hotspotY: Int) {

        if (serial != enterSerial) {
            return
        }

        var cursor: Cursor? = this.cursors[wlPointerResource]
        val hotspot = Point.create(hotspotX,
                hotspotY)

        if (cursor == null) {
            cursor = this.cursorFactory.create(wlSurfaceResource,
                    hotspot)

            val wlSurface = wlSurfaceResource.implementation as WlSurface
            val surface = wlSurface.surface
            val view = surface.createView(wlSurfaceResource,
                    this.position)
            this.scene.cursorLayer
                    .surfaceViews
                    .add(view)

            this.cursors.put(wlPointerResource,
                    cursor)
            wlPointerResource.register {
                Optional.ofNullable(this.cursors.remove(wlPointerResource))
                        .ifPresent(Consumer<Cursor> { it.hide() })
            }
        } else {
            cursor.wlSurfaceResource = wlSurfaceResource
            cursor.hotspot = hotspot
        }

        cursor.show()
        updateActiveCursor(wlPointerResource)

        val wlSurface = wlSurfaceResource.implementation as WlSurface
        val surface = wlSurface.surface

        val stateBuilder = surface.state
                .toBuilder()
        updateCursorSurfaceState(wlSurfaceResource,
                stateBuilder)
        surface.state = stateBuilder.build()
    }

    private fun updateActiveCursor(wlPointerResource: WlPointerResource) {

        val cursor = this.cursors[wlPointerResource]
        val oldCursor = this.activeCursor
        this.activeCursor = Optional.ofNullable(cursor)

        this.activeCursor.ifPresent { clientCursor -> clientCursor.updatePosition(position) }

        if (oldCursor != this.activeCursor) {
            oldCursor.ifPresent { it.hide() }
        }
    }

    private fun updateCursorSurfaceState(wlSurfaceResource: WlSurfaceResource,
                                         surfaceStateBuilder: SurfaceState.Builder) {

        surfaceStateBuilder.inputRegion(Optional.of(this.nullRegion))
        if (this.activeCursor.isPresent &&
                this.activeCursor.get().wlSurfaceResource == wlSurfaceResource &&
                !this.activeCursor.get().isHidden) {
        } else {
            surfaceStateBuilder.buffer(Optional.empty())
        }
    }

    override fun beforeCommit(wlSurfaceResource: WlSurfaceResource) {
        val wlSurface = wlSurfaceResource.implementation as WlSurface
        val surface = wlSurface.surface

        updateCursorSurfaceState(wlSurfaceResource,
                surface.pendingState)
    }

    override fun afterDestroy(wlSurfaceResource: WlSurfaceResource) {
        this.cursors.values.removeIf {
            if (it.wlSurfaceResource == wlSurfaceResource) {
                it.hide()
                return @this.cursors.values.removeIf true
            } else {
                return @this.cursors.values.removeIf false
            }
        }
    }

    //TODO unit test

    /**
     * Limit the pointer new position to the clamp region.

     * @param newPosition the desired new position
     * *
     * *
     * @see .getClampRegion
     */
    fun clamp(wlPointerResources: Set<WlPointerResource>,
              newPosition: Point) {
        warp(wlPointerResources,
                this.geo.clamp(this.position,
                        newPosition,
                        this.clampRegion))
    }

    //TODO unit test
    fun warp(wlPointerResources: Set<WlPointerResource>,
             position: Point) {
        this.position = position
        if (!grab.isPresent) {
            calculateFocus(wlPointerResources)
        }
    }

    override fun accept(roleVisitor: RoleVisitor) {
        roleVisitor.visit(this)
    }
}