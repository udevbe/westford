package org.westmalle.wayland.nativ.libbcm_host;


import com.sun.jna.Structure;

import java.util.Collections;
import java.util.List;

public class DISPMANX_CLAMP_T extends Structure {

    private static final List<?> FIELD_ORDER = Collections.singletonList("dummy");

    public int dummy;

    @Override
    protected List<?> getFieldOrder() { return FIELD_ORDER; }
}
