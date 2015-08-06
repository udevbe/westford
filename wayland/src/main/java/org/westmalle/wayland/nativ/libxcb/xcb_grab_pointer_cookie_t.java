package org.westmalle.wayland.nativ.libxcb;

import com.sun.jna.Structure;

import java.util.Collections;
import java.util.List;

public class xcb_grab_pointer_cookie_t extends Structure {

    private static final List<?> FIELD_ORDER = Collections.singletonList("sequence");

    public int sequence;

    protected List<?> getFieldOrder() {
        return FIELD_ORDER;
    }

    public static class ByValue extends xcb_grab_pointer_cookie_t implements Structure.ByValue {

    }
}
