package org.westmalle.wayland.nativ.libbcm_host;

import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

public class VC_RECT_T extends Structure {

    private static final List<?> FIELD_ORDER = Arrays.asList("x",
                                                             "y",
                                                             "width",
                                                             "height");

    public int x;
    public int y;
    public int width;
    public int height;

    @Override
    protected List<?> getFieldOrder() { return FIELD_ORDER; }
}
