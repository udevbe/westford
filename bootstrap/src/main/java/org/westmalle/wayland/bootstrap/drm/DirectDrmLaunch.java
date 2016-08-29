package org.westmalle.wayland.bootstrap.drm;


public class DirectDrmLaunch {
    public static void main(final String[] args) throws Exception {
        new DrmBoot().strap(DaggerDirectDrmEglCompositor.create());
    }
}
