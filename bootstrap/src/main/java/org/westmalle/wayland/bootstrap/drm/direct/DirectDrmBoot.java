package org.westmalle.wayland.bootstrap.drm.direct;


import org.westmalle.wayland.bootstrap.drm.DrmBoot;

public class DirectDrmBoot {
    public static void main(final String[] args) {
        new DrmBoot().strap(DaggerDirectDrmEglCompositor.INSTANCE);
    }
}
