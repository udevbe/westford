package org.westmalle.wayland.nativ;


import com.sun.jna.NativeLong;
import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

public class pixman_region32_data extends Structure {

    private static final List<?> FIELD_ORDER = Arrays.asList("size",
                                                             "numRects");

    public NativeLong size;
    public NativeLong numRects;

    public pixman_region32_data() {
        super();
    }

    protected List<?> getFieldOrder() {
        return FIELD_ORDER;
    }

    public static class ByReference extends pixman_region32_data implements Structure.ByReference {
    }
}
