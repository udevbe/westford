package org.westmalle.wayland.nativ;


import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

public class xcb_button_press_event_t extends Structure {

    private static List<?> FIELD_ORDER = Arrays.asList("response_type",
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
                                                       "same_screen",
                                                       "pad0");

    public byte  response_type;
    public byte  detail;
    public short sequence;
    public int   time;
    public int   root;
    public int   event;
    public int   child;
    public short root_x;
    public short root_y;
    public short event_x;
    public short event_y;
    public short state;
    public byte  same_screen;
    public byte  pad0;

    @Override
    protected List<?> getFieldOrder() {
        return FIELD_ORDER;
    }
}
