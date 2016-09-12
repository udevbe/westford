package org.westmalle.wayland.bootstrap.drm.indirect;

import org.westmalle.wayland.bootstrap.drm.DrmBoot;

public class IndirectDrmBoot {
    public static void main(final String[] args) {
        new DrmBoot().strap(DaggerIndirectDrmEglCompositor.create());
    }
}
