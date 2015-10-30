package org.westmalle.wayland.nativ.libbcm_host;

import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

public class DISPMANX_MODEINFO_T extends Structure {

    private static final List<String> FIELD_ORDER = Arrays.asList("width",
                                                                  "height",
                                                                  "transform",
                                                                  "input_format",
                                                                  "display_num");

    public int width;
    public int height;
    public int transform;
    public int input_format;
    public int display_num;

    @Override
    protected List getFieldOrder() {
        return FIELD_ORDER;
    }
}
