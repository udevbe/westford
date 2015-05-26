package org.westmalle.wayland.nativ;


import com.sun.jna.NativeLong;
import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

public class pixman_region32_data extends Structure {
    public NativeLong size;
    public NativeLong numRects;

    public pixman_region32_data() {
        super();
    }

    protected List<?> getFieldOrder() {
        return Arrays.asList("size",
                             "numRects");
    }
}
