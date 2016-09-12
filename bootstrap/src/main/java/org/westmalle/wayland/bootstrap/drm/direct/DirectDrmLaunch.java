package org.westmalle.wayland.bootstrap.drm.direct;


public class DirectDrmLaunch {
    public static void main(final String[] args) throws Exception {
        DaggerDirectDrmEglCompositor.INSTANCE.launcher()
                                             .launch(DirectDrmBoot.class,
                                                     args);
    }
}
