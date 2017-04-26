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
package org.westford.nativ.libinput

import org.freedesktop.jaccall.Lib
import org.freedesktop.jaccall.Ptr
import org.freedesktop.jaccall.Unsigned

import javax.inject.Singleton

@Singleton
@Lib(value = "input", version = 10)
class Libinput {

    @Ptr
    external fun libinput_udev_create_context(@Ptr(libinput_interface::class) interface_: Long,
                                              @Ptr(Void::class) user_data: Long,
                                              @Ptr udev: Long): Long

    @Ptr
    external fun libinput_unref(@Ptr libinput: Long): Long

    external fun libinput_udev_assign_seat(@Ptr libinput: Long,
                                           @Ptr(String::class) seat_id: Long): Int

    external fun libinput_log_set_handler(@Ptr libinput: Long,
                                          @Ptr(libinput_log_handler::class) log_handler: Long)

    external fun libinput_log_set_priority(@Ptr libinput: Long,
                                           priority: Int)

    external fun libinput_dispatch(@Ptr libinput: Long): Int

    @Ptr
    external fun libinput_get_event(@Ptr libinput: Long): Long

    @Ptr
    external fun libinput_event_destroy(@Ptr libinput: Long): Long

    external fun libinput_get_fd(@Ptr libinput: Long): Int

    external fun libinput_event_get_type(@Ptr event: Long): Int

    @Ptr
    external fun libinput_event_get_keyboard_event(@Ptr event: Long): Long

    @Ptr
    external fun libinput_event_get_pointer_event(@Ptr event: Long): Long

    @Ptr
    external fun libinput_event_get_touch_event(@Ptr event: Long): Long

    @Unsigned
    external fun libinput_event_keyboard_get_time(@Ptr event: Long): Int


    external fun libinput_event_keyboard_get_key_state(@Ptr event: Long): Int

    @Unsigned
    external fun libinput_event_keyboard_get_seat_key_count(@Ptr event: Long): Int

    @Unsigned
    external fun libinput_event_keyboard_get_key(@Ptr event: Long): Int

    external fun libinput_event_pointer_get_dx(@Ptr event: Long): Double

    external fun libinput_event_pointer_get_dy(@Ptr event: Long): Double

    external fun libinput_event_pointer_get_absolute_x_transformed(@Ptr event: Long,
                                                                   @Unsigned width: Int): Double

    external fun libinput_event_pointer_get_absolute_y_transformed(@Ptr event: Long,
                                                                   @Unsigned height: Int): Double

    @Unsigned
    external fun libinput_event_pointer_get_button(@Ptr event: Long): Int

    external fun libinput_event_pointer_get_button_state(@Ptr event: Long): Int

    @Unsigned
    external fun libinput_event_pointer_get_seat_button_count(@Ptr event: Long): Int

    @Unsigned
    external fun libinput_event_pointer_get_time(@Ptr event: Long): Int


    external fun libinput_event_pointer_has_axis(@Ptr event: Long,
                                                 axis: Int): Int

    external fun libinput_event_pointer_get_axis_source(@Ptr event: Long): Int

    external fun libinput_event_pointer_get_axis_value_discrete(@Ptr event: Long,
                                                                axis: Int): Double

    external fun libinput_event_pointer_get_axis_value(@Ptr event: Long,
                                                       axis: Int): Double


    external fun libinput_event_touch_get_seat_slot(@Ptr event: Long): Int

    @Unsigned
    external fun libinput_event_touch_get_time(@Ptr event: Long): Int

    external fun libinput_event_touch_get_x_transformed(@Ptr event: Long,
                                                        @Unsigned width: Int): Double

    external fun libinput_event_touch_get_y_transformed(@Ptr event: Long,
                                                        @Unsigned height: Int): Double

    @Ptr
    external fun libinput_event_get_device(@Ptr event: Long): Long


    external fun libinput_device_has_capability(@Ptr device: Long,
                                                capability: Int): Int

    external fun libinput_device_set_user_data(@Ptr device: Long,
                                               @Ptr(Void::class) user_data: Long)

    @Ptr(Void::class)
    external fun libinput_device_get_user_data(@Ptr device: Long): Long


    @Ptr(String::class)
    external fun libinput_device_get_output_name(@Ptr device: Long): Long

    @Ptr
    external fun libinput_device_ref(@Ptr device: Long): Long

    @Ptr
    external fun libinput_device_unref(@Ptr device: Long): Long

    companion object {

        /**
         * The event is caused by the rotation of a wheel.
         */
        val LIBINPUT_POINTER_AXIS_SOURCE_WHEEL = 1
        /**
         * The event is caused by the movement of one or more fingers on a
         * device.
         */
        val LIBINPUT_POINTER_AXIS_SOURCE_FINGER = 2
        /**
         * The event is caused by the motion of some device.
         */
        val LIBINPUT_POINTER_AXIS_SOURCE_CONTINUOUS = 3

        /**
         * Axes on a device with the capability @ref LIBINPUT_DEVICE_CAP_POINTER
         * that are not x or y coordinates.
         *
         *
         * The two scroll axes LIBINPUT_POINTER_AXIS_SCROLL_VERTICAL and
         * LIBINPUT_POINTER_AXIS_SCROLL_HORIZONTAL are engaged separately,
         * depending on the device. libinput provides some scroll direction locking
         * but it is up to the caller to determine which axis is needed and
         * appropriate in the current interaction
         */
        val LIBINPUT_POINTER_AXIS_SCROLL_VERTICAL = 0
        val LIBINPUT_POINTER_AXIS_SCROLL_HORIZONTAL = 1

        /**
         * Logical state of a key. Note that the logical state may not represent
         * the physical state of the key.
         */
        val LIBINPUT_KEY_STATE_RELEASED = 0
        val LIBINPUT_KEY_STATE_PRESSED = 1


        /**
         * Logical state of a physical button. Note that the logical state may not
         * represent the physical state of the button.
         */
        val LIBINPUT_BUTTON_STATE_RELEASED = 0
        val LIBINPUT_BUTTON_STATE_PRESSED = 1


        /**
         * This is not a real event type, and is only used to tell the user that
         * no new event is available in the queue. See
         * libinput_next_event_type().
         */
        val LIBINPUT_EVENT_NONE = 0

        /**
         * Signals that a device has been added to the context. The device will
         * not be read until the next time the user calls libinput_dispatch()
         * and data is available.
         *
         *
         * This allows setting up initial device configuration before any events
         * are created.
         */
        val LIBINPUT_EVENT_DEVICE_ADDED = 1

        /**
         * Signals that a device has been removed. No more events from the
         * associated device will be in the queue or be queued after this event.
         */
        val LIBINPUT_EVENT_DEVICE_REMOVED = 2

        val LIBINPUT_EVENT_KEYBOARD_KEY = 300

        val LIBINPUT_EVENT_POINTER_MOTION = 400
        val LIBINPUT_EVENT_POINTER_MOTION_ABSOLUTE = 401
        val LIBINPUT_EVENT_POINTER_BUTTON = 402
        val LIBINPUT_EVENT_POINTER_AXIS = 403

        val LIBINPUT_EVENT_TOUCH_DOWN = 500
        val LIBINPUT_EVENT_TOUCH_UP = 501
        val LIBINPUT_EVENT_TOUCH_MOTION = 502
        val LIBINPUT_EVENT_TOUCH_CANCEL = 503
        /**
         * Signals the end of a set of touchpoints at one device sample
         * time. This event has no coordinate information attached.
         */
        val LIBINPUT_EVENT_TOUCH_FRAME = 504

        /**
         * One or more axes have changed state on a device with the @ref
         * LIBINPUT_DEVICE_CAP_TABLET_TOOL capability. This event is only sent
         * when the tool is in proximity, see @ref
         * LIBINPUT_EVENT_TABLET_TOOL_PROXIMITY for details.
         *
         *
         * The proximity event contains the initial state of the axis as the
         * tool comes into proximity. An event of type @ref
         * LIBINPUT_EVENT_TABLET_TOOL_AXIS is only sent when an axis value
         * changes from this initial state. It is possible for a tool to
         * enter and leave proximity without sending an event of type @ref
         * LIBINPUT_EVENT_TABLET_TOOL_AXIS.
         *
         *
         * An event of type @ref LIBINPUT_EVENT_TABLET_TOOL_AXIS is sent
         * when the tip state does not change. See the documentation for

         * @ref LIBINPUT_EVENT_TABLET_TOOL_TIP for more details.
         */
        val LIBINPUT_EVENT_TABLET_TOOL_AXIS = 600
        /**
         * Signals that a tool has come in or out of proximity of a device with
         * the @ref LIBINPUT_DEVICE_CAP_TABLET_TOOL capability.
         *
         *
         * Proximity events contain each of the current values for each axis,
         * and these values may be extracted from them in the same way they are
         * with @ref LIBINPUT_EVENT_TABLET_TOOL_AXIS events.
         *
         *
         * Some tools may always be in proximity. For these tools, events of
         * type @ref LIBINPUT_TABLET_TOOL_PROXIMITY_STATE_IN are sent only once after @ref
         * LIBINPUT_EVENT_DEVICE_ADDED, and events of type @ref
         * LIBINPUT_TABLET_TOOL_PROXIMITY_STATE_OUT are sent only once before @ref
         * LIBINPUT_EVENT_DEVICE_REMOVED.
         *
         *
         * If the tool that comes into proximity supports x/y coordinates,
         * libinput guarantees that both x and y are set in the proximity
         * event.
         *
         *
         * When a tool goes out of proximity, the value of every axis should be
         * assumed to have an undefined state and any buttons that are currently held
         * down on the stylus are marked as released. Button release events for
         * each button that was held down on the stylus are sent before the
         * proximity out event.
         */
        val LIBINPUT_EVENT_TABLET_TOOL_PROXIMITY = 601
        /**
         * Signals that a tool has come in contact with the surface of a
         * device with the @ref LIBINPUT_DEVICE_CAP_TABLET_TOOL capability.
         *
         *
         * On devices without distance proximity detection, the @ref
         * LIBINPUT_EVENT_TABLET_TOOL_TIP is sent immediately after @ref
         * LIBINPUT_EVENT_TABLET_TOOL_PROXIMITY for the tip down event, and
         * immediately before for the tip up event.
         *
         *
         * The decision when a tip touches the surface is device-dependent
         * and may be derived from pressure data or other means. If the tip
         * state is changed by axes changing state, the

         * @ref LIBINPUT_EVENT_TABLET_TOOL_TIP event includes the changed
         * * axes and no additional axis event is sent for this state change.
         * * In other words, a caller must look at both @ref
         * * LIBINPUT_EVENT_TABLET_TOOL_AXIS and @ref
         * * LIBINPUT_EVENT_TABLET_TOOL_TIP events to know the current state
         * * of the axes.
         * *
         *
         *
         * * If a button state change occurs at the same time as a tip state
         * * change, the order of events is device-dependent.
         */
        val LIBINPUT_EVENT_TABLET_TOOL_TIP = 602
        /**
         * Signals that a tool has changed a logical button state on a
         * device with the @ref LIBINPUT_DEVICE_CAP_TABLET_TOOL capability.
         *
         *
         * Button state changes occur on their own and do not include axis
         * state changes. If button and axis state changes occur within the
         * same logical hardware event, the order of the @ref
         * LIBINPUT_EVENT_TABLET_TOOL_BUTTON and @ref
         * LIBINPUT_EVENT_TABLET_TOOL_AXIS event is device-specific.
         *
         *
         * This event is not to be confused with the button events emitted
         * by the tablet pad. See @ref LIBINPUT_EVENT_TABLET_PAD_BUTTON.
         */
        val LIBINPUT_EVENT_TABLET_TOOL_BUTTON = 603

        /**
         * A button pressed on a device with the @ref
         * LIBINPUT_DEVICE_CAP_TABLET_PAD capability.
         *
         *
         * This event is not to be confused with the button events emitted
         * by tools on a tablet. See @ref LIBINPUT_EVENT_TABLET_TOOL_BUTTON.
         */
        val LIBINPUT_EVENT_TABLET_PAD_BUTTON = 700
        /**
         * A status change on a tablet ring with the
         * LIBINPUT_DEVICE_CAP_TABLET_PAD capability.
         */
        val LIBINPUT_EVENT_TABLET_PAD_RING = 701

        /**
         * A status change on a strip on a device with the @ref
         * LIBINPUT_DEVICE_CAP_TABLET_PAD capability.
         */
        val LIBINPUT_EVENT_TABLET_PAD_STRIP = 702

        val LIBINPUT_EVENT_GESTURE_SWIPE_BEGIN = 800
        val LIBINPUT_EVENT_GESTURE_SWIPE_UPDATE = 801
        val LIBINPUT_EVENT_GESTURE_SWIPE_END = 802
        val LIBINPUT_EVENT_GESTURE_PINCH_BEGIN = 803
        val LIBINPUT_EVENT_GESTURE_PINCH_UPDATE = 804
        val LIBINPUT_EVENT_GESTURE_PINCH_END = 805

        val LIBINPUT_DEVICE_CAP_KEYBOARD = 0
        val LIBINPUT_DEVICE_CAP_POINTER = 1
        val LIBINPUT_DEVICE_CAP_TOUCH = 2
        val LIBINPUT_DEVICE_CAP_TABLET_TOOL = 3
        val LIBINPUT_DEVICE_CAP_TABLET_PAD = 4
        val LIBINPUT_DEVICE_CAP_GESTURE = 5
    }

}
