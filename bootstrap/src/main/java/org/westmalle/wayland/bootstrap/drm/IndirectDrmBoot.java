package org.westmalle.wayland.bootstrap.drm;


public class IndirectDrmBoot {
    public static void main(final String[] args) {
        new DrmBoot().strap(DaggerIndirectDrmEglCompositor.create());
    }
}
