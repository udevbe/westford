package org.westmalle.wayland.nativ.libxcb;

import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

public class xcb_generic_event_t extends Structure {

    private static final List<?> FIELD_ORDER = Arrays.asList("response_type",
                                                             "pad0",
                                                             "sequence",
                                                             "pad",
                                                             "full_sequence");

    public byte  response_type;
    public byte  pad0;
    public short sequence;
    public int[] pad = new int[7];
    public int full_sequence;

    @Override
    protected List<?> getFieldOrder() {
        return FIELD_ORDER;
    }
}
