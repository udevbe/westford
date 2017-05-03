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
package org.westford.compositor.input

import com.google.auto.factory.AutoFactory
import com.google.auto.factory.Provided
import org.freedesktop.jaccall.Pointer
import org.freedesktop.wayland.shared.WlKeyboardKeyState
import org.freedesktop.wayland.shared.WlPointerAxis
import org.freedesktop.wayland.shared.WlPointerAxisSource
import org.freedesktop.wayland.shared.WlPointerButtonState
import org.westford.compositor.core.RenderPlatform
import org.westford.compositor.protocol.WlOutput
import org.westford.compositor.protocol.WlSeat
import org.westford.nativ.libinput.Libinput
import org.westford.nativ.libinput.Libinput.Companion.LIBINPUT_BUTTON_STATE_PRESSED
import org.westford.nativ.libinput.Libinput.Companion.LIBINPUT_BUTTON_STATE_RELEASED
import org.westford.nativ.libinput.Libinput.Companion.LIBINPUT_KEY_STATE_PRESSED
import org.westford.nativ.libinput.Libinput.Companion.LIBINPUT_KEY_STATE_RELEASED
import org.westford.nativ.libinput.Libinput.Companion.LIBINPUT_POINTER_AXIS_SCROLL_HORIZONTAL
import org.westford.nativ.libinput.Libinput.Companion.LIBINPUT_POINTER_AXIS_SCROLL_VERTICAL
import org.westford.nativ.libinput.Libinput.Companion.LIBINPUT_POINTER_AXIS_SOURCE_CONTINUOUS
import org.westford.nativ.libinput.Libinput.Companion.LIBINPUT_POINTER_AXIS_SOURCE_FINGER
import org.westford.nativ.libinput.Libinput.Companion.LIBINPUT_POINTER_AXIS_SOURCE_WHEEL

@AutoFactory(className = "LibinputDeviceFactory",
             allowSubclasses = true) class LibinputDevice(@param:Provided private val libinput: Libinput,
                                                          @param:Provided private val renderPlatform: RenderPlatform,
                                                          private val wlSeat: WlSeat,
                                                          private val device: Long,
                                                          val deviceCapabilities: Int) {

    fun handleKeyboardKey(keyboardEvent: Long) {

        val time = this.libinput.libinput_event_keyboard_get_time(keyboardEvent)
        val key = this.libinput.libinput_event_keyboard_get_key(keyboardEvent)
        val keyState = this.libinput.libinput_event_keyboard_get_key_state(keyboardEvent)
        val seatKeyCount = this.libinput.libinput_event_keyboard_get_seat_key_count(keyboardEvent)

        if (keyState == LIBINPUT_KEY_STATE_PRESSED && seatKeyCount != 1 || keyState == LIBINPUT_KEY_STATE_RELEASED && seatKeyCount != 0) {
            //don't send key events when we have an additional press or release of the same key on the same seat from a different device.
            return
        }

        val wlKeyboard = this.wlSeat.wlKeyboard
        wlKeyboard.keyboardDevice.key(wlKeyboard.resources,
                                      time,
                                      key,
                                      wlKeyboardKeyState(keyState))
    }

    private fun wlKeyboardKeyState(keyState: Int): WlKeyboardKeyState {
        val wlKeyboardKeyState: WlKeyboardKeyState
        if (keyState == LIBINPUT_KEY_STATE_PRESSED) {
            wlKeyboardKeyState = WlKeyboardKeyState.PRESSED
        }
        else {
            wlKeyboardKeyState = WlKeyboardKeyState.RELEASED
        }
        return wlKeyboardKeyState
    }

    fun handlePointerMotion(pointerEvent: Long) {

        val time = this.libinput.libinput_event_pointer_get_time(pointerEvent)
        val dx = this.libinput.libinput_event_pointer_get_dx(pointerEvent)
        val dy = this.libinput.libinput_event_pointer_get_dy(pointerEvent)

        val wlPointer = this.wlSeat.wlPointer
        val pointerDevice = wlPointer.pointerDevice
        val pointerDevicePosition = pointerDevice.position

        pointerDevice.motion(wlPointer.resources,
                             time,
                             pointerDevicePosition.x + dx.toInt(),
                             pointerDevicePosition.y + dy.toInt())
        pointerDevice.frame(wlPointer.resources)
    }

    fun handlePointerMotionAbsolute(pointerEvent: Long) {
        findBoundOutput()?.let {
            //FIXME we should to take into account that boundOutput pixel size is not always the same as compositor coordinates but for now it is.

            val geometry = it.output.geometry
            val physicalWidth = geometry.physicalWidth
            val physicalHeight = geometry.physicalHeight

            val time = this.libinput.libinput_event_pointer_get_time(pointerEvent)
            val x = this.libinput.libinput_event_pointer_get_absolute_x_transformed(pointerEvent,
                                                                                    physicalWidth)
            val y = this.libinput.libinput_event_pointer_get_absolute_y_transformed(pointerEvent,
                                                                                    physicalHeight)

            val wlPointer = this.wlSeat.wlPointer
            val pointerDevice = wlPointer.pointerDevice

            pointerDevice.motion(wlPointer.resources,
                                 time,
                                 x.toInt(),
                                 y.toInt())
            pointerDevice.frame(wlPointer.resources)
        }
    }

    fun findBoundOutput(): WlOutput? {
        //TODO we can cache the output that is mapped to this device and listen for output detsruction/addition so we save a few nanoseconds

        val outputNamePointer = this.libinput.libinput_device_get_output_name(this.device)
        if (outputNamePointer == 0L) {
            val iterator = this.renderPlatform.wlOutputs.iterator()
            if (iterator.hasNext()) {
                return iterator.next()
            }
            else {
                return null
            }
        }

        val deviceOutputName = Pointer.wrap<String>(String::class.java,
                                                    outputNamePointer).get()
        //        for (final WlOutput wlOutput : this.renderPlatform.getWlOutput()) {
        //FIXME give outputs a name, iterate them and match
        //            if (deviceOutputName.equals(renderPlatform.getOutput()
        //                                                .getName())) {
        return this.renderPlatform.wlOutputs[0]
        //            }
        //     }

        //      return Optional.empty();
    }

    fun handlePointerButton(pointerEvent: Long) {

        val time = this.libinput.libinput_event_pointer_get_time(pointerEvent)
        val buttonState = this.libinput.libinput_event_pointer_get_button_state(pointerEvent)
        val seatButtonCount = this.libinput.libinput_event_pointer_get_seat_button_count(pointerEvent)
        val button = this.libinput.libinput_event_pointer_get_button(pointerEvent)

        if (buttonState == LIBINPUT_BUTTON_STATE_PRESSED && seatButtonCount != 1 || buttonState == LIBINPUT_BUTTON_STATE_RELEASED && seatButtonCount != 0) {
            //don't send button events when we have an additional press or release of the same key on the same seat from a different device.
            return
        }

        val wlPointer = this.wlSeat.wlPointer
        val pointerDevice = wlPointer.pointerDevice

        pointerDevice.button(wlPointer.resources,
                             time,
                             button,
                             wlPointerButtonState(buttonState))
        pointerDevice.frame(wlPointer.resources)
    }

    private fun wlPointerButtonState(buttonState: Int): WlPointerButtonState {
        if (buttonState == LIBINPUT_BUTTON_STATE_PRESSED) {
            return WlPointerButtonState.PRESSED
        }
        else {
            return WlPointerButtonState.RELEASED
        }
    }

    fun handlePointerAxis(pointerEvent: Long) {

        val hasVertical = this.libinput.libinput_event_pointer_has_axis(pointerEvent,
                                                                        LIBINPUT_POINTER_AXIS_SCROLL_VERTICAL)
        val hasHorizontal = this.libinput.libinput_event_pointer_has_axis(pointerEvent,
                                                                          LIBINPUT_POINTER_AXIS_SCROLL_HORIZONTAL)

        if (hasVertical == 0 && hasHorizontal == 0) {
            return
        }

        val source = this.libinput.libinput_event_pointer_get_axis_source(pointerEvent)
        val wlPointerAxisSource: WlPointerAxisSource

        when (source) {
            LIBINPUT_POINTER_AXIS_SOURCE_WHEEL      -> wlPointerAxisSource = WlPointerAxisSource.WHEEL
            LIBINPUT_POINTER_AXIS_SOURCE_FINGER     -> wlPointerAxisSource = WlPointerAxisSource.FINGER
            LIBINPUT_POINTER_AXIS_SOURCE_CONTINUOUS -> wlPointerAxisSource = WlPointerAxisSource.CONTINUOUS
            else                                    -> //unknown scroll source
                return
        }

        val wlPointer = this.wlSeat.wlPointer
        val pointerDevice = wlPointer.pointerDevice

        pointerDevice.axisSource(wlPointer.resources,
                                 wlPointerAxisSource)

        if (hasVertical != 0) {
            val vertDiscrete = getAxisDiscrete(pointerEvent,
                                               LIBINPUT_POINTER_AXIS_SCROLL_VERTICAL)
            val vert = normalizeScroll(pointerEvent,
                                       LIBINPUT_POINTER_AXIS_SCROLL_VERTICAL)

            val time = this.libinput.libinput_event_pointer_get_time(pointerEvent)

            if (vertDiscrete == 0) {
                pointerDevice.axisContinuous(wlPointer.resources,
                                             time,
                                             WlPointerAxis.VERTICAL_SCROLL,
                                             vert.toFloat())
            }
            else {
                pointerDevice.axisDiscrete(wlPointer.resources,
                                           WlPointerAxis.VERTICAL_SCROLL,
                                           time,
                                           vertDiscrete,
                                           vert.toFloat())
            }
        }

        if (hasHorizontal != 0) {
            val horizDiscrete = getAxisDiscrete(pointerEvent,
                                                LIBINPUT_POINTER_AXIS_SCROLL_HORIZONTAL)
            val horiz = normalizeScroll(pointerEvent,
                                        LIBINPUT_POINTER_AXIS_SCROLL_HORIZONTAL)

            val time = this.libinput.libinput_event_pointer_get_time(pointerEvent)

            if (horizDiscrete == 0) {
                pointerDevice.axisContinuous(wlPointer.resources,
                                             time,
                                             WlPointerAxis.HORIZONTAL_SCROLL,
                                             horiz.toFloat())
            }
            else {
                pointerDevice.axisDiscrete(wlPointer.resources,
                                           WlPointerAxis.HORIZONTAL_SCROLL,
                                           time,
                                           horizDiscrete,
                                           horiz.toFloat())
            }
        }

        pointerDevice.frame(wlPointer.resources)
    }

    private fun getAxisDiscrete(pointerEvent: Long,
                                axis: Int): Int {
        val source = this.libinput.libinput_event_pointer_get_axis_source(pointerEvent)

        if (source != LIBINPUT_POINTER_AXIS_SOURCE_WHEEL) {
            return 0
        }

        return this.libinput.libinput_event_pointer_get_axis_value_discrete(pointerEvent,
                                                                            axis).toInt()
    }

    private fun normalizeScroll(pointerEvent: Long,
                                axis: Int): Double {
        var value = 0.0

        val source = this.libinput.libinput_event_pointer_get_axis_source(pointerEvent)
        /* libinput < 0.8 sent wheel click events with value 10. Since 0.8
       the value is the angle of the click in degrees. To keep
	   backwards-compat with existing clients, we just send multiples of
	   the click count.
	 */
        when (source) {
            LIBINPUT_POINTER_AXIS_SOURCE_WHEEL                                           -> value = 10 * this.libinput.libinput_event_pointer_get_axis_value_discrete(pointerEvent,
                                                                                                                                                                      axis)
            LIBINPUT_POINTER_AXIS_SOURCE_FINGER, LIBINPUT_POINTER_AXIS_SOURCE_CONTINUOUS -> value = this.libinput.libinput_event_pointer_get_axis_value(pointerEvent,
                                                                                                                                                        axis)
        }

        return value
    }

    fun handleTouchDown(touchEvent: Long) {
        findBoundOutput()?.let {
            //FIXME we should to take into account that boundOutput pixel size != compositor coordinates

            val outputGeometry = it.output.geometry
            val physicalWidth = outputGeometry.physicalWidth
            val physicalHeight = outputGeometry.physicalHeight

            val time = this.libinput.libinput_event_touch_get_time(touchEvent)
            val slot = this.libinput.libinput_event_touch_get_seat_slot(touchEvent)
            val x = this.libinput.libinput_event_touch_get_x_transformed(touchEvent,
                                                                         physicalWidth).toInt()
            val y = this.libinput.libinput_event_touch_get_y_transformed(touchEvent,
                                                                         physicalHeight).toInt()

            val wlTouch = this.wlSeat.wlTouch
            wlTouch.touchDevice.down(wlTouch.resources,
                                     slot,
                                     time,
                                     x,
                                     y)
        }
    }

    fun handleTouchMotion(touchEvent: Long) {
        findBoundOutput()?.let {
            //FIXME we should to take into account that boundOutput pixel size is not always the same as compositor coordinates but for now it is.

            val outputGeometry = it.output.geometry
            val physicalWidth = outputGeometry.physicalWidth
            val physicalHeight = outputGeometry.physicalHeight

            val time = this.libinput.libinput_event_touch_get_time(touchEvent)
            val slot = this.libinput.libinput_event_touch_get_seat_slot(touchEvent)
            val x = this.libinput.libinput_event_touch_get_x_transformed(touchEvent,
                                                                         physicalWidth).toInt()
            val y = this.libinput.libinput_event_touch_get_y_transformed(touchEvent,
                                                                         physicalHeight).toInt()

            val wlTouch = this.wlSeat.wlTouch
            wlTouch.touchDevice.motion(wlTouch.resources,
                                       slot,
                                       time,
                                       x,
                                       y)
        }
    }

    fun handleTouchUp(touchEvent: Long) {
        val time = this.libinput.libinput_event_touch_get_time(touchEvent)
        val slot = this.libinput.libinput_event_touch_get_seat_slot(touchEvent)

        val wlTouch = this.wlSeat.wlTouch
        wlTouch.touchDevice.up(wlTouch.resources,
                               slot,
                               time)
    }

    fun handleTouchFrame(touchEvent: Long) {
        val wlTouch = this.wlSeat.wlTouch
        wlTouch.touchDevice.frame(wlTouch.resources)
    }
}
