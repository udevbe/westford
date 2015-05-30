package org.westmalle.wayland.nativ;


import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

public class xcb_generic_error_t extends Structure {

    private static final List<?> FIELD_ORDER = Arrays.asList("response_type",
                                                             "error_code",
                                                             "sequence",
                                                             "resource_id",
                                                             "minor_code",
                                                             "major_code",
                                                             "pad0",
                                                             "pad",
                                                             "full_sequence");

    public byte  response_type;
    public byte  error_code;
    public short sequence;
    public int   resource_id;
    public short minor_code;
    public byte  major_code;
    public byte  pad0;
    public int[] pad = new int[5];
    public int full_sequence;

    @Override
    protected List<?> getFieldOrder() {
        return FIELD_ORDER;
    }
}
