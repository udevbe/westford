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
package org.westford.compositor.x11

import com.google.auto.factory.AutoFactory
import com.google.auto.factory.Provided
import org.freedesktop.wayland.shared.WlKeyboardKeyState
import org.freedesktop.wayland.shared.WlPointerAxis
import org.freedesktop.wayland.shared.WlPointerAxis.HORIZONTAL_SCROLL
import org.freedesktop.wayland.shared.WlPointerAxis.VERTICAL_SCROLL
import org.freedesktop.wayland.shared.WlPointerButtonState
import org.westford.compositor.core.Point
import org.westford.compositor.protocol.WlSeat
import org.westford.nativ.libxcb.Libxcb
import org.westford.nativ.libxcb.Libxcb.Companion.XCB_CURSOR_NONE
import org.westford.nativ.libxcb.Libxcb.Companion.XCB_EVENT_MASK_BUTTON_PRESS
import org.westford.nativ.libxcb.Libxcb.Companion.XCB_EVENT_MASK_BUTTON_RELEASE
import org.westford.nativ.libxcb.Libxcb.Companion.XCB_EVENT_MASK_ENTER_WINDOW
import org.westford.nativ.libxcb.Libxcb.Companion.XCB_EVENT_MASK_LEAVE_WINDOW
import org.westford.nativ.libxcb.Libxcb.Companion.XCB_EVENT_MASK_POINTER_MOTION
import org.westford.nativ.libxcb.Libxcb.Companion.XCB_GRAB_MODE_ASYNC
import org.westford.nativ.linux.InputEventCodes.BTN_LEFT
import org.westford.nativ.linux.InputEventCodes.BTN_MIDDLE
import org.westford.nativ.linux.InputEventCodes.BTN_RIGHT
import org.westford.nativ.linux.InputEventCodes.BTN_SIDE

@AutoFactory(className = "PrivateX11SeatFactory",
             allowSubclasses = true) class X11Seat(@param:Provided private val libxcb: Libxcb,
                                                   @param:Provided private val x11Platform: X11Platform,
                                                   val wlSeat: WlSeat) {

    fun deliverKey(time: Int,
                   eventDetail: Short,
                   pressed: Boolean) {
        val wlKeyboardKeyState = wlKeyboardKeyState(pressed)
        val key = toLinuxKey(eventDetail)
        val wlKeyboard = this.wlSeat.wlKeyboard

        wlKeyboard.keyboardDevice.key(wlKeyboard.resources,
                                      time,
                                      key,
                                      wlKeyboardKeyState)
    }

    private fun wlKeyboardKeyState(pressed: Boolean): WlKeyboardKeyState {
        val wlKeyboardKeyState: WlKeyboardKeyState
        if (pressed) {
            wlKeyboardKeyState = WlKeyboardKeyState.PRESSED
        }
        else {
            wlKeyboardKeyState = WlKeyboardKeyState.RELEASED
        }
        return wlKeyboardKeyState
    }

    //convert from X keycodes to input.h keycodes
    private fun toLinuxKey(eventDetail: Short): Int = eventDetail - 8

    fun deliverButton(window: Int,
                      buttonTime: Int,
                      eventDetail: Short,
                      pressed: Boolean) {

        val wlPointerButtonState = wlPointerButtonState(window,
                                                        buttonTime,
                                                        pressed)
        val button = toLinuxButton(eventDetail.toInt())
        if (button == 0 && pressed) {
            handleScroll(buttonTime,
                         eventDetail)
        }
        else if (button != 0) {
            val wlPointer = this.wlSeat.wlPointer
            val pointerDevice = wlPointer.pointerDevice

            pointerDevice.button(wlPointer.resources,
                                 buttonTime,
                                 button,
                                 wlPointerButtonState)
            pointerDevice.frame(wlPointer.resources)
        }
    }

    private fun wlPointerButtonState(window: Int,
                                     buttonTime: Int,
                                     pressed: Boolean): WlPointerButtonState {
        val wlPointerButtonState: WlPointerButtonState
        if (pressed) {
            wlPointerButtonState = WlPointerButtonState.PRESSED
            this.libxcb.xcb_grab_pointer(this.x11Platform.xcbConnection,
                                         0.toByte(),
                                         window,
                                         (XCB_EVENT_MASK_BUTTON_PRESS or XCB_EVENT_MASK_BUTTON_RELEASE or XCB_EVENT_MASK_POINTER_MOTION or XCB_EVENT_MASK_ENTER_WINDOW or XCB_EVENT_MASK_LEAVE_WINDOW).toShort(),
                                         XCB_GRAB_MODE_ASYNC.toByte(),
                                         XCB_GRAB_MODE_ASYNC.toByte(),
                                         window,
                                         XCB_CURSOR_NONE,
                                         buttonTime)
        }
        else {
            this.libxcb.xcb_ungrab_pointer(this.x11Platform.xcbConnection,
                                           buttonTime)
            wlPointerButtonState = WlPointerButtonState.RELEASED
        }
        return wlPointerButtonState
    }

    private fun toLinuxButton(eventDetail: Int): Int {
        val button: Int
        when (eventDetail) {
            1          -> button = BTN_LEFT
            2          -> button = BTN_MIDDLE
            3          -> button = BTN_RIGHT
            4, 5, 6, 7 ->
                //scroll
                button = 0
            else       -> button = eventDetail + BTN_SIDE - 8
        }
        return button
    }

    private fun handleScroll(buttonTime: Int,
                             eventDetail: Short) {

        val wlPointerAxis: WlPointerAxis
        val value: Float
        val discreteValue: Int

        if (eventDetail.toInt() == 4 || eventDetail.toInt() == 5) {
            wlPointerAxis = VERTICAL_SCROLL
            value = if (eventDetail.toInt() == 4) -DEFAULT_AXIS_STEP_DISTANCE else DEFAULT_AXIS_STEP_DISTANCE
            discreteValue = if (eventDetail.toInt() == 4) -1 else 1
        }
        else {
            wlPointerAxis = HORIZONTAL_SCROLL
            value = if (eventDetail.toInt() == 6) -DEFAULT_AXIS_STEP_DISTANCE else DEFAULT_AXIS_STEP_DISTANCE
            discreteValue = if (eventDetail.toInt() == 6) -1 else 1
        }

        val wlPointer = this.wlSeat.wlPointer
        val pointerDevice = wlPointer.pointerDevice

        pointerDevice.axisDiscrete(wlPointer.resources,
                                   wlPointerAxis,
                                   buttonTime,
                                   discreteValue,
                                   value)
        pointerDevice.frame(wlPointer.resources)
    }

    fun deliverMotion(windowId: Int,
                      time: Int,
                      x: Int,
                      y: Int) {
        this.x11Platform.renderOutputs.forEach {
            if (it.xWindow == windowId) {

                val point = toGlobal(it,
                                     x,
                                     y)

                val wlPointer = this.wlSeat.wlPointer
                val pointerDevice = wlPointer.pointerDevice

                pointerDevice.motion(wlPointer.resources,
                                     time,
                                     point.x,
                                     point.y)
                pointerDevice.frame(wlPointer.resources)
            }
        }
    }

    private fun toGlobal(x11Output: X11Output,
                         x11WindowX: Int,
                         x11WindowY: Int): Point {
        val globalX = x11Output.x + x11WindowX
        val globalY = x11Output.y + x11WindowY

        return Point(globalX,
                     globalY)
    }

    companion object {

        private val DEFAULT_AXIS_STEP_DISTANCE = 10.0f
    }
}
