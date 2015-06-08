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

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

public class xcb_leave_notify_event_t extends Structure {

    private static final List<?> FIELD_ORDER = Arrays.asList("response_type",
                                                             "detail",
                                                             "sequence",
                                                             "time",
                                                             "root",
                                                             "event",
                                                             "child",
                                                             "root_x",
                                                             "root_y",
                                                             "event_x",
                                                             "event_y",
                                                             "state",
                                                             "mode",
                                                             "same_screen_focus");
    public byte response_type;
    public byte detail;
    public short sequence;
    public int time;
    public int root;
    public int event;
    public int child;
    public short root_x;
    public short root_y;
    public short event_x;
    public short event_y;
    public short state;
    public byte mode;
    public byte same_screen_focus;

    public xcb_leave_notify_event_t(final Pointer p) {
        super(p);
    }

    @Override
    protected List<?> getFieldOrder() {
        return FIELD_ORDER;
    }
}
