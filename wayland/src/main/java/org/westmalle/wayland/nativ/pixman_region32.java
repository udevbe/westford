package org.westmalle.wayland.nativ;

import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

public class pixman_region32 extends Structure {

    private static final List<?> FIELD_ORDER = Arrays.asList("extents",
                                                             "data");

    /**
     * C type : pixman_box32_t
     */
    public pixman_box32                     extents;
    /**
     * C type : pixman_region32_data_t*
     */
    public pixman_region32_data.ByReference data;

    public pixman_region32() {
        super();
    }

    protected List<?> getFieldOrder() {
        return FIELD_ORDER;
    }
}