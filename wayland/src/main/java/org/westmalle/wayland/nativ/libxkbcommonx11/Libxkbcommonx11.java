/*
 * Westmalle Wayland Compositor.
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
package org.westmalle.wayland.nativ.libxkbcommonx11;

import org.freedesktop.jaccall.Lib;
import org.freedesktop.jaccall.Ptr;

import javax.annotation.Nonnegative;
import javax.inject.Singleton;

@Singleton
@Lib(value = "xkbcommon-x11",
     version = 0)
public class Libxkbcommonx11 {

    /**
     * Get the keyboard device ID of the core X11 keyboard.
     *
     * @param connection An XCB connection to the X server.
     *
     * @return A device ID which may be used with other xkb_x11_* functions, or -1 on failure.
     */
    public native int xkb_x11_get_core_keyboard_device_id(@Ptr long connection);

    /**
     * Create a keymap from an X11 keyboard device.
     * <p/>
     * This function queries the X server with various requests, fetches the details of the active keymap on a keyboard
     * device, and creates an xkb_keymap from these details.
     *
     * @param context    The context in which to create the keymap.
     * @param connection An XCB connection to the X server.
     * @param device_id  An XInput 1 device ID (in the range 0-255) with input class KEY. Passing values outside of this
     *                   range is an error.
     * @param flags      Optional flags for the keymap, or 0.
     *
     * @return A keymap retrieved from the X server, or NULL on failure.
     */
    @Ptr
    public native long xkb_x11_keymap_new_from_device(@Ptr long context,
                                                      @Ptr long connection,
                                                      @Nonnegative int device_id,
                                                      int flags);

    /**
     * Create a new keyboard state object from an X11 keyboard device.
     * <p/>
     * This function is the same as xkb_state_new(), only pre-initialized with the state of the device at the time this
     * function is called.
     *
     * @param keymap     The keymap for which to create the state.
     * @param connection An XCB connection to the X server.
     * @param device_id  An XInput 1 device ID (in the range 0-255) with input class KEY. Passing values outside of this
     *                   range is an error.
     *
     * @return A new keyboard state object, or NULL on failure.
     */
    @Ptr
    public native long xkb_x11_state_new_from_device(@Ptr long keymap,
                                                     @Ptr long connection,
                                                     @Nonnegative int device_id);
}
