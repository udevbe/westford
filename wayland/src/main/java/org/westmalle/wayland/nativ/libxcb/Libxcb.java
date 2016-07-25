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
package org.westmalle.wayland.nativ.libxcb;

import org.freedesktop.jaccall.ByVal;
import org.freedesktop.jaccall.Lib;
import org.freedesktop.jaccall.Ptr;

import javax.inject.Singleton;
import java.util.Optional;

@Singleton
@Lib(value = "xcb",
     version = 1)
public class Libxcb {

    public static final int  XCB_ATOM_ATOM                 = 4;
    public static final int  XCB_PROP_MODE_REPLACE         = 0;
    public static final int  XCB_GRAB_MODE_ASYNC           = 1;
    public static final int  XCB_CURSOR_NONE               = 0;
    public static final long XCB_COPY_FROM_PARENT          = 0L;
    public static final int  XCB_WINDOW_CLASS_INPUT_OUTPUT = 1;
    public static final int  XCB_CW_EVENT_MASK             = 2048;
    public static final int  XCB_EVENT_MASK_KEY_PRESS      = 1;
    public static final int  XCB_EVENT_MASK_KEY_RELEASE    = 2;
    public static final int  XCB_EVENT_MASK_BUTTON_PRESS   = 4;
    public static final int  XCB_EVENT_MASK_BUTTON_RELEASE = 8;
    public static final int  XCB_EVENT_MASK_ENTER_WINDOW   = 16;
    public static final int  XCB_EVENT_MASK_LEAVE_WINDOW   = 32;
    public static final int  XCB_EVENT_MASK_POINTER_MOTION = 64;
    public static final int  XCB_EVENT_MASK_KEYMAP_STATE   = 16384;
    public static final int  XCB_EVENT_MASK_FOCUS_CHANGE   = 2097152;
    public static final int  XCB_KEY_PRESS                 = 2;
    public static final int  XCB_KEY_RELEASE               = 3;
    public static final int  XCB_BUTTON_PRESS              = 4;
    public static final int  XCB_BUTTON_RELEASE            = 5;
    public static final int  XCB_MOTION_NOTIFY             = 6;
    public static final int  XCB_ENTER_NOTIFY              = 7;
    public static final int  XCB_LEAVE_NOTIFY              = 8;
    public static final int  XCB_FOCUS_IN                  = 9;
    public static final int  XCB_FOCUS_OUT                 = 10;
    public static final int  XCB_KEYMAP_NOTIFY             = 11;
    public static final int  XCB_EXPOSE                    = 12;
    public static final int  XCB_CLIENT_MESSAGE            = 33;
    public static final int  XCB_MAPPING_NOTIFY            = 34;
    public static final int  XCB_MAPPING_MODIFIER          = 0;
    public static final int  XCB_MAPPING_KEYBOARD          = 1;
    public static final int  XCB_MAPPING_POINTER           = 2;


    public native int xcb_flush(@Ptr long c);

    @Ptr
    public native long xcb_get_setup(@Ptr long c);

    @ByVal(xcb_screen_iterator_t.class)
    public native long xcb_setup_roots_iterator(@Ptr long setup);

    public native int xcb_connection_has_error(@Ptr long c);

    public native int xcb_generate_id(@Ptr long c);

    public native int xcb_create_window(@Ptr long c,
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
                                        @Ptr long value_list);

    public native int xcb_map_window(@Ptr long c,
                                     int wid);

    @Ptr
    public native long xcb_poll_for_event(@Ptr long c);

    public native int xcb_get_file_descriptor(@Ptr long c);

    public native int xcb_grab_pointer(@Ptr long c,
                                       byte owner_events,
                                       int grab_window,
                                       short event_mask,
                                       byte pointer_mode,
                                       byte keyboard_mode,
                                       int confine_to,
                                       int cursor,
                                       int time);

    public native int xcb_ungrab_pointer(@Ptr long c,
                                         int time);

    public native int xcb_intern_atom(@Ptr long c,
                                      byte only_if_exists,
                                      short name_len,
                                      @Ptr long name);

    @Ptr
    public native long xcb_intern_atom_reply(@Ptr long c,
                                             int cookie,
                                             @Ptr long e);

    public native int xcb_change_property(@Ptr long c,
                                          byte mode,
                                          int window,
                                          int property,
                                          int type,
                                          byte format,
                                          int data_len,
                                          @Ptr long data);

    public native int xcb_destroy_window(@Ptr long c,
                                         int window);
}
