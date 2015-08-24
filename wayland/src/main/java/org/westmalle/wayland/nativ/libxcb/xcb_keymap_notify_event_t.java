package org.westmalle.wayland.nativ.libxcb;


import com.sun.jna.Pointer;
import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

public class xcb_keymap_notify_event_t extends Structure {

    private static final List<?> FIELD_ORDER = Arrays.asList("response_type",
                                                             "keys");

    public byte response_type;
    public byte[] keys = new byte[31];

    public xcb_keymap_notify_event_t(final Pointer pointer) {
        super(pointer);
    }

    public xcb_keymap_notify_event_t() {
    }

    @Override
    protected List<?> getFieldOrder() {
        return FIELD_ORDER;
    }
}
