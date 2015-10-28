package org.westmalle.wayland.nativ.libbcm_host;

import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

public class EGL_DISPMANX_WINDOW_T extends Structure {

    private static final List<?> FIELD_ORDER = Arrays.asList("element",
                                                             "width",
                                                             "height");

    public int element;
    public int width;
    public int height;

    @Override
    protected List<?> getFieldOrder() { return FIELD_ORDER; }
}
