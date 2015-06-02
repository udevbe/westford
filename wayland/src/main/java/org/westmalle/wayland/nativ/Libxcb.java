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
package org.westmalle.wayland.nativ;

import com.sun.jna.Native;
import com.sun.jna.Pointer;

import javax.inject.Singleton;

@Singleton
public class Libxcb {

    static {
        Native.register("xcb");
    }

    Libxcb() {
    }

    public static final long XCB_COPY_FROM_PARENT          = 0L;
    public static final int  XCB_WINDOW_CLASS_INPUT_OUTPUT = 1;
    public static final int  XCB_CW_EVENT_MASK             = 2048;

    public static final int XCB_EVENT_MASK_KEY_PRESS           = 1;
    public static final int XCB_EVENT_MASK_KEY_RELEASE         = 2;
    public static final int XCB_EVENT_MASK_BUTTON_PRESS        = 4;
    public static final int XCB_EVENT_MASK_BUTTON_RELEASE      = 8;
    public static final int XCB_EVENT_MASK_ENTER_WINDOW        = 16;
    public static final int XCB_EVENT_MASK_LEAVE_WINDOW        = 32;
    public static final int XCB_EVENT_MASK_POINTER_MOTION      = 64;
    public static final int XCB_EVENT_MASK_POINTER_MOTION_HINT = 128;
    public static final int XCB_EVENT_MASK_BUTTON_MOTION       = 8192;
    public static final int XCB_EVENT_MASK_EXPOSURE            = 32768;

    public static final int XCB_KEY_PRESS      = 2;
    public static final int XCB_KEY_RELEASE    = 3;
    public static final int XCB_BUTTON_PRESS   = 4;
    public static final int XCB_BUTTON_RELEASE = 5;
    public static final int XCB_MOTION_NOTIFY  = 6;
    public static final int XCB_ENTER_NOTIFY   = 7;
    public static final int XCB_LEAVE_NOTIFY   = 8;

    public native int xcb_flush(Pointer c);

    public native Pointer xcb_get_setup(Pointer c);

    public native xcb_screen_iterator_t.ByValue xcb_setup_roots_iterator(Pointer setup);

    public native int xcb_connection_has_error(Pointer c);

    public native int xcb_generate_id(Pointer c);

    public native xcb_void_cookie_t.ByValue xcb_create_window_checked(Pointer c,
                                                                      byte depth,
                                                                      int wid,
                                                                      int parent,
                                                                      short x,
                                                                      short y,
                                                                      short width,
                                                                      short height,
                                                                      short border_width,
                                                                      short _class,
                                                                      int visual,
                                                                      int value_mask,
                                                                      Pointer value_list);

    public native xcb_void_cookie_t.ByValue xcb_map_window_checked(Pointer c,
                                                                   int wid);

    public native xcb_generic_event_t xcb_poll_for_event(Pointer c);

    public native xcb_generic_error_t xcb_request_check(Pointer c,
                                                        xcb_void_cookie_t cookie);

    public native int xcb_get_file_descriptor(Pointer c);
}
