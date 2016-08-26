package org.westmalle.wayland.bootstrap.drm;


import org.westmalle.launch.DaggerLauncherComponent;
import org.westmalle.launch.Launcher;

public class DrmLauncher {
    public static void main(final String[] args) throws Exception {
        //TODO use args to decide if we should do a direct launch or not(?)
        final Launcher launcher = DaggerLauncherComponent.create()
                                                         .direct()
                                                         .launcher();
        launcher.launch(DrmBoot.class,
                        args);
    }
}
