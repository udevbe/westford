package org.westmalle.wayland.platform.eglkms.drm;

import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

public class drmModeModeInfo extends Structure{
    public int clock;
    public short hdisplay, hsync_start, hsync_end, htotal, hskew;
    public short vdisplay, vsync_start, vsync_end, vtotal, vscan;

    public int vrefresh;

    public int flags;
    public int type;
    public byte[] name = new byte[32];

    @Override
    protected List getFieldOrder() {
        return Arrays.asList("clock",
                             "hdisplay","hsync_start","hsync_end","htotal","hskew",
                             "vdisplay","vsync_start","vsync_end","vtotal","vscan",
                             "vrefresh","flags","type","name");
    }

    public static class ByReference extends drmModeModeInfo implements Structure.ByReference {

    }
}
