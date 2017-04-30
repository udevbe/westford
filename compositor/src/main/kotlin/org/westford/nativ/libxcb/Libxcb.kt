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
package org.westford.nativ.libxcb

import org.freedesktop.jaccall.ByVal
import org.freedesktop.jaccall.Lib
import org.freedesktop.jaccall.Ptr

import javax.inject.Singleton

@Singleton @Lib(value = "xcb",
                version = 1) class Libxcb {

    external fun xcb_flush(@Ptr c: Long): Int

    @Ptr external fun xcb_get_setup(@Ptr c: Long): Long

    @ByVal(xcb_screen_iterator_t::class) external fun xcb_setup_roots_iterator(@Ptr setup: Long): Long

    external fun xcb_connection_has_error(@Ptr c: Long): Int

    external fun xcb_generate_id(@Ptr c: Long): Int

    external fun xcb_create_window(@Ptr c: Long,
                                   depth: Byte,
                                   wid: Int,
                                   parent: Int,
                                   x: Short,
                                   y: Short,
                                   width: Short,
                                   height: Short,
                                   border_width: Short,
                                   _class: Short,
                                   visual: Int,
                                   value_mask: Int,
                                   @Ptr value_list: Long): Int

    external fun xcb_map_window(@Ptr c: Long,
                                wid: Int): Int

    @Ptr external fun xcb_poll_for_event(@Ptr c: Long): Long

    external fun xcb_get_file_descriptor(@Ptr c: Long): Int

    external fun xcb_grab_pointer(@Ptr c: Long,
                                  owner_events: Byte,
                                  grab_window: Int,
                                  event_mask: Short,
                                  pointer_mode: Byte,
                                  keyboard_mode: Byte,
                                  confine_to: Int,
                                  cursor: Int,
                                  time: Int): Int

    external fun xcb_ungrab_pointer(@Ptr c: Long,
                                    time: Int): Int

    external fun xcb_intern_atom(@Ptr c: Long,
                                 only_if_exists: Byte,
                                 name_len: Short,
                                 @Ptr name: Long): Int

    @Ptr external fun xcb_intern_atom_reply(@Ptr c: Long,
                                            cookie: Int,
                                            @Ptr e: Long): Long

    external fun xcb_change_property(@Ptr c: Long,
                                     mode: Byte,
                                     window: Int,
                                     property: Int,
                                     type: Int,
                                     format: Byte,
                                     data_len: Int,
                                     @Ptr data: Long): Int

    external fun xcb_destroy_window(@Ptr c: Long,
                                    window: Int): Int

    companion object {

        val XCB_ATOM_ATOM = 4
        val XCB_PROP_MODE_REPLACE = 0
        val XCB_GRAB_MODE_ASYNC = 1
        val XCB_CURSOR_NONE = 0
        val XCB_COPY_FROM_PARENT = 0L
        val XCB_WINDOW_CLASS_INPUT_OUTPUT = 1
        val XCB_CW_EVENT_MASK = 2048
        val XCB_EVENT_MASK_KEY_PRESS = 1
        val XCB_EVENT_MASK_KEY_RELEASE = 2
        val XCB_EVENT_MASK_BUTTON_PRESS = 4
        val XCB_EVENT_MASK_BUTTON_RELEASE = 8
        val XCB_EVENT_MASK_ENTER_WINDOW = 16
        val XCB_EVENT_MASK_LEAVE_WINDOW = 32
        val XCB_EVENT_MASK_POINTER_MOTION = 64
        val XCB_EVENT_MASK_KEYMAP_STATE = 16384
        val XCB_EVENT_MASK_FOCUS_CHANGE = 2097152
        val XCB_KEY_PRESS = 2
        val XCB_KEY_RELEASE = 3
        val XCB_BUTTON_PRESS = 4
        val XCB_BUTTON_RELEASE = 5
        val XCB_MOTION_NOTIFY = 6
        val XCB_ENTER_NOTIFY = 7
        val XCB_LEAVE_NOTIFY = 8
        val XCB_FOCUS_IN = 9
        val XCB_FOCUS_OUT = 10
        val XCB_KEYMAP_NOTIFY = 11
        val XCB_EXPOSE = 12
        val XCB_CLIENT_MESSAGE = 33
        val XCB_MAPPING_NOTIFY = 34
        val XCB_MAPPING_MODIFIER = 0
        val XCB_MAPPING_KEYBOARD = 1
        val XCB_MAPPING_POINTER = 2
    }
}
