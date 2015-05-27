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
package org.westmalle.wayland.x11;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;

import org.westmalle.wayland.nativ.LibX11;
import org.westmalle.wayland.nativ.Libxcb;
import org.westmalle.wayland.nativ.xcb_generic_error_t;
import org.westmalle.wayland.nativ.xcb_screen_t;
import org.westmalle.wayland.nativ.xcb_void_cookie_t;
import org.westmalle.wayland.protocol.WlOutput;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.inject.Inject;

import static com.google.common.base.Preconditions.checkArgument;


public class X11OutputFactory {

    private final LibX11 libX11;
    private final Libxcb libxcb;

    @Inject
    X11OutputFactory(final LibX11 libX11,
                     final Libxcb libxcb) {
        this.libX11 = libX11;
        this.libxcb = libxcb;
    }

    public WlOutput create(@Nonnull final String xDisplay,
                           @Nonnegative final int width,
                           @Nonnegative final int height) {
        checkArgument(width > 0);
        checkArgument(height > 0);

        return createWlOutput(xDisplay,
                              width,
                              height);
    }

    private WlOutput createWlOutput(final String xDisplay,
                                    final int width,
                                    final int height) {

        Pointer display = this.libX11.XOpenDisplay(xDisplay);
        if (display == null) {
            throw new RuntimeException("XOpenDisplay() failed: " + xDisplay);
        }

        Pointer connection = this.libX11.XGetXCBConnection(display);
        if (connection == null) {
            throw new RuntimeException("XGetXCBConnection() failed");
        }
        if (this.libxcb.xcb_connection_has_error(connection) != 0) {
            throw new RuntimeException("errors occured in connecting to X server");
        }

        Pointer setup = this.libxcb.xcb_get_setup(connection);
        xcb_screen_t screen = this.libxcb.xcb_setup_roots_iterator(setup).data;

        int window = this.libxcb.xcb_generate_id(connection);
        if (window <= 0) {
            throw new RuntimeException("failed to generate X window id");
        }

        int xcb_window_attrib_mask = this.libxcb.XCB_CW_EVENT_MASK;
        Pointer xcb_window_attrib_list = new Memory(4 * 3);
        xcb_window_attrib_list.setInt(0, this.libxcb.XCB_EVENT_MASK_BUTTON_PRESS);
        xcb_window_attrib_list.setInt(4, this.libxcb.XCB_EVENT_MASK_EXPOSURE);
        xcb_window_attrib_list.setInt(8, this.libxcb.XCB_EVENT_MASK_KEY_PRESS);

        xcb_void_cookie_t create_cookie = this.libxcb.xcb_create_window_checked(
                connection,
                (byte) this.libxcb.XCB_COPY_FROM_PARENT, // depth
                window,
                screen.root, // parent window
                (short) 0,
                (short) 0,
                (short) width,
                (short) height,
                (short) 0, // border width
                (short) this.libxcb.XCB_WINDOW_CLASS_INPUT_OUTPUT, // class
                screen.root_visual, // visual
                xcb_window_attrib_mask,
                xcb_window_attrib_list);

        xcb_void_cookie_t map_cookie = this.libxcb.xcb_map_window_checked(connection, window);

        // Check errors.
        xcb_generic_error_t error;
        error = this.libxcb.xcb_request_check(connection, create_cookie);
        if (error != null) {
            throw new RuntimeException("failed to create X window: " + error.error_code);
        }
        error = this.libxcb.xcb_request_check(connection, map_cookie);
        if (error != null) {
            throw new RuntimeException("failed to map X window: " + error.error_code);
        }

        return null;
    }
}
