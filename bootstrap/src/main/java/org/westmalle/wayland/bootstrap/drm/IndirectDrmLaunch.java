package org.westmalle.wayland.bootstrap.drm;


public class IndirectDrmLaunch {
    public static void main(final String[] args) throws Exception {
        DaggerIndirectDrmEglCompositor.create()
                                      .launcher()
                                      .launch(IndirectDrmBoot.class,
                                              args);
    }
}
