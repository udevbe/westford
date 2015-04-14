package org.westmalle.wayland.platform.eglkms.drm;

import com.sun.jna.Structure;

import java.util.List;

public class drmModeModeInfo extends Structure{
    int clock;
    short hdisplay, hsync_start, hsync_end, htotal, hskew;
    short vdisplay, vsync_start, vsync_end, vtotal, vscan;

    int vrefresh;

    int flags;
    int type;
    byte[] name = new byte[32];

    @Override
    protected List getFieldOrder() {
        return null;
    }

    public static class ByReference extends drmModeModeInfo implements Structure.ByReference {

    }
}
