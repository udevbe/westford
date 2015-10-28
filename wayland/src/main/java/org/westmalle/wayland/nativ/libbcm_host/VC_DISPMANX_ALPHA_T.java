package org.westmalle.wayland.nativ.libbcm_host;

import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

public class VC_DISPMANX_ALPHA_T extends Structure {

    private static final List<?> FIELD_ORDER = Arrays.asList("flags",
                                                             "opacity",
                                                             "mask");

    public int flags;
    public int opacity;
    public int mask;

    @Override
    protected List<?> getFieldOrder() { return FIELD_ORDER; }
}
