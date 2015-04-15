package org.westmalle.wayland.platform.newt.eglkms.drm;


import com.sun.jna.Pointer;
import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

public class drmModeRes extends Structure {

    public int count_fbs;
    public Pointer fbs;

    public int count_crtcs;
    public Pointer crtcs;

    public int count_connectors;
    public Pointer connectors;

    public int count_encoders;
    public Pointer encoders;

    public int min_width, max_width;
    public int min_height, max_height;

    @Override
    protected List getFieldOrder() {
        return Arrays.asList("count_fbs","fbs","count_crtcs","crtcs","count_connectors","connectors","count_encoders",
                             "encoders","min_width","max_width","min_height","max_height");
    }
}