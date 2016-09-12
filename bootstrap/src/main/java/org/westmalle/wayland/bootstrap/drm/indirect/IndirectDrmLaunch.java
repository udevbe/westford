package org.westmalle.wayland.bootstrap.drm.indirect;

public class IndirectDrmLaunch {
    public static void main(final String[] args) throws Exception {
        DaggerIndirectDrmEglCompositor.create()
                                      .launcher()
                                      .launch(IndirectDrmBoot.class,
                                              args);
    }
}
