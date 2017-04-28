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
import org.freedesktop.jaccall.Pointer.wrap
import org.freedesktop.wayland.server.Display
import org.freedesktop.wayland.server.EventSource
import org.freedesktop.wayland.server.jaccall.WaylandServerCore
import org.freedesktop.wayland.shared.WlSeatCapability
import org.freedesktop.wayland.shared.WlSeatCapability.KEYBOARD
import org.freedesktop.wayland.shared.WlSeatCapability.POINTER
import org.freedesktop.wayland.shared.WlSeatCapability.TOUCH
import org.westford.compositor.protocol.WlSeat
import org.westford.nativ.libinput.Libinput
import org.westford.nativ.libinput.Libinput.Companion.LIBINPUT_DEVICE_CAP_KEYBOARD
import org.westford.nativ.libinput.Libinput.Companion.LIBINPUT_DEVICE_CAP_POINTER
import org.westford.nativ.libinput.Libinput.Companion.LIBINPUT_DEVICE_CAP_TOUCH
import org.westford.nativ.libinput.Libinput.Companion.LIBINPUT_EVENT_DEVICE_ADDED
import org.westford.nativ.libinput.Libinput.Companion.LIBINPUT_EVENT_DEVICE_REMOVED
import org.westford.nativ.libinput.Libinput.Companion.LIBINPUT_EVENT_KEYBOARD_KEY
import org.westford.nativ.libinput.Libinput.Companion.LIBINPUT_EVENT_NONE
import org.westford.nativ.libinput.Libinput.Companion.LIBINPUT_EVENT_POINTER_AXIS
import org.westford.nativ.libinput.Libinput.Companion.LIBINPUT_EVENT_POINTER_BUTTON
import org.westford.nativ.libinput.Libinput.Companion.LIBINPUT_EVENT_POINTER_MOTION
import org.westford.nativ.libinput.Libinput.Companion.LIBINPUT_EVENT_POINTER_MOTION_ABSOLUTE
import org.westford.nativ.libinput.Libinput.Companion.LIBINPUT_EVENT_TOUCH_DOWN
import org.westford.nativ.libinput.Libinput.Companion.LIBINPUT_EVENT_TOUCH_FRAME
import org.westford.nativ.libinput.Libinput.Companion.LIBINPUT_EVENT_TOUCH_MOTION
import org.westford.nativ.libinput.Libinput.Companion.LIBINPUT_EVENT_TOUCH_UP
import java.util.*

@AutoFactory(allowSubclasses = true,
             className = "PrivateLibinputSeatFactory") class LibinputSeat(@param:Provided private val display: Display,
                                                                          @param:Provided private val libinput: Libinput,
                                                                          @param:Provided private val libinputDeviceFactory: LibinputDeviceFactory,
                                                                          private val libinputContext: Long,
                                                                          private val wlSeat: WlSeat) {

    private val libinputDevices = HashSet<LibinputDevice>()
    private var inputEventSource = Optional.empty<EventSource>()

    fun disableInput() {
        this.inputEventSource.ifPresent { eventSource ->
            eventSource.remove()
            this.inputEventSource = Optional.empty<EventSource>()
        }
    }

    fun enableInput() {
        loop(this.libinputContext)
    }

    private fun loop(libinput: Long) {
        if (!this.inputEventSource.isPresent) {
            val libinputFd = this.libinput.libinput_get_fd(libinput)
            this.inputEventSource = Optional.of(this.display.eventLoop.addFileDescriptor(libinputFd,
                                                                                         WaylandServerCore.WL_EVENT_READABLE) { fd, mask ->
                if (fd == libinputFd) {
                    processEvents(libinput)
                }
                0
            })
        }
    }

    private fun processEvents(libinput: Long) {
        this.libinput.libinput_dispatch(libinput)

        var event: Long
        while ((event = this.libinput.libinput_get_event(libinput)) != 0) {

            processEvent(event)

            this.libinput.libinput_event_destroy(event)
            this.libinput.libinput_dispatch(libinput)
        }
    }

    private fun processEvent(event: Long) {
        val eventType = this.libinput.libinput_event_get_type(event)
        val device = this.libinput.libinput_event_get_device(event)
        when (eventType) {
            LIBINPUT_EVENT_NONE           -> {
            }
            LIBINPUT_EVENT_DEVICE_ADDED   -> handleDeviceAdded(device)
            LIBINPUT_EVENT_DEVICE_REMOVED -> handleDeviceRemoved(device)
            else                          -> processDeviceEvent(event,
                                                                eventType,
                                                                device)
        }//no more events
    }

    private fun handleDeviceAdded(device: Long) {
        //check device capabilities, if it's not a touch, pointer or keyboard, we're not interested.
        val deviceCapabilities = EnumSet.noneOf<WlSeatCapability>(WlSeatCapability::class.java)

        if (this.libinput.libinput_device_has_capability(device,
                                                         LIBINPUT_DEVICE_CAP_KEYBOARD) != 0) {
            deviceCapabilities.add(KEYBOARD)
        }
        if (this.libinput.libinput_device_has_capability(device,
                                                         LIBINPUT_DEVICE_CAP_POINTER) != 0) {
            deviceCapabilities.add(POINTER)
        }
        if (this.libinput.libinput_device_has_capability(device,
                                                         LIBINPUT_DEVICE_CAP_TOUCH) != 0) {
            deviceCapabilities.add(TOUCH)
        }

        if (deviceCapabilities.isEmpty()) {
            return
        }

        //TODO configure device

        val libinputDevice = this.libinputDeviceFactory.create(this.wlSeat,
                                                               device,
                                                               deviceCapabilities)
        this.libinput.libinput_device_set_user_data(device,
                                                    Pointer.from(libinputDevice).address)
        this.libinput.libinput_device_ref(device)
        this.libinputDevices.add(libinputDevice)

        emitSeatCapabilities()
    }

    private fun handleDeviceRemoved(device: Long) {
        val deviceData = this.libinput.libinput_device_get_user_data(device)
        if (deviceData == 0L) {
            //device is not handled by us
            return
        }

        val devicePointer = wrap<LibinputDevice>(LibinputDevice::class.java,
                                                 deviceData)
        val libinputDevice = devicePointer.dref()
        this.libinputDevices.remove(libinputDevice)
        devicePointer.close()
        this.libinput.libinput_device_unref(device)

        emitSeatCapabilities()
    }

    private fun processDeviceEvent(event: Long,
                                   eventType: Int,
                                   device: Long) {
        val deviceData = this.libinput.libinput_device_get_user_data(device)
        if (deviceData == 0L) {
            //device was not mapped to a device we can handle
            return
        }
        val libinputDevice = wrap<Any>(Any::class.java,
                                       deviceData).dref() as LibinputDevice

        when (eventType) {
            LIBINPUT_EVENT_KEYBOARD_KEY            -> libinputDevice.handleKeyboardKey(this.libinput.libinput_event_get_keyboard_event(event))
            LIBINPUT_EVENT_POINTER_MOTION          -> libinputDevice.handlePointerMotion(this.libinput.libinput_event_get_pointer_event(event))
            LIBINPUT_EVENT_POINTER_MOTION_ABSOLUTE -> libinputDevice.handlePointerMotionAbsolute(this.libinput.libinput_event_get_pointer_event(event))
            LIBINPUT_EVENT_POINTER_BUTTON          -> libinputDevice.handlePointerButton(this.libinput.libinput_event_get_pointer_event(event))
            LIBINPUT_EVENT_POINTER_AXIS            -> libinputDevice.handlePointerAxis(this.libinput.libinput_event_get_pointer_event(event))
            LIBINPUT_EVENT_TOUCH_DOWN              -> libinputDevice.handleTouchDown(this.libinput.libinput_event_get_touch_event(event))
            LIBINPUT_EVENT_TOUCH_MOTION            -> libinputDevice.handleTouchMotion(this.libinput.libinput_event_get_touch_event(event))
            LIBINPUT_EVENT_TOUCH_UP                -> libinputDevice.handleTouchUp(this.libinput.libinput_event_get_touch_event(event))
            LIBINPUT_EVENT_TOUCH_FRAME             -> libinputDevice.handleTouchFrame(this.libinput.libinput_event_get_touch_event(event))
            else                                   -> {
            }
        }//unsupported libinput event
    }

    private fun emitSeatCapabilities() {

        val seatCapabilities = EnumSet.noneOf<WlSeatCapability>(WlSeatCapability::class.java)

        for (libinputDevice in this.libinputDevices) {
            seatCapabilities.addAll(libinputDevice.deviceCapabilities)
        }

        val seat = this.wlSeat.seat
        seat.setCapabilities(seatCapabilities)
        seat.emitCapabilities(this.wlSeat.getResources())
    }
}
