//Copyright 2015 Erik De Rijcke
//
//Licensed under the Apache License,Version2.0(the"License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
//http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing,software
//distributed under the License is distributed on an"AS IS"BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
package org.westmalle.wayland.nativ.libxkbcommonx11;


import com.sun.jna.Pointer;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Libxkbcommonx11 {

    /**
     * Get the keyboard device ID of the core X11 keyboard.
     *
     * @param connection An XCB connection to the X server.
     *
     * @return A device ID which may be used with other xkb_x11_* functions, or -1 on failure.
     */
    public native int xkb_x11_get_core_keyboard_device_id(@Nonnull Pointer connection);

    /**
     * Create a keymap from an X11 keyboard device.
     * <p>
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
    @Nullable
    public native Pointer xkb_x11_keymap_new_from_device(@Nonnull Pointer context,
                                                         @Nonnull Pointer connection,
                                                         @Nonnegative int device_id,
                                                         int flags);

    /**
     * Create a new keyboard state object from an X11 keyboard device.
     * <p>
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
    @Nullable
    public native Pointer xkb_x11_state_new_from_device(@Nonnull Pointer keymap,
                                                        @Nonnull Pointer connection,
                                                        @Nonnegative int device_id);
}
