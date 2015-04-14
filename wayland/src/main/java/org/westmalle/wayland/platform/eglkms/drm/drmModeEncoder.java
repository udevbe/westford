package org.westmalle.wayland.platform.eglkms.drm;

import com.sun.jna.Structure;

import java.util.List;

public class drmModeEncoder extends Structure{

    public int encoder_id;
    public int encoder_type;
    public int crtc_id;
    public int possible_crtcs;
    public int possible_clones;

    @Override
    protected List getFieldOrder() {
        return null;
    }
}
