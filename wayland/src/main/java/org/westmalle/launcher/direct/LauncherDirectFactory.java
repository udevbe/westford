package org.westmalle.launcher.direct;


import org.westmalle.launcher.Launcher;
import org.westmalle.tty.Tty;

import javax.annotation.Nonnull;
import javax.inject.Inject;

public class LauncherDirectFactory {

    @Nonnull
    private final Tty                             tty;
    @Nonnull
    private final PrivateDrmLauncherDirectFactory privateDrmLauncherDirectFactory;

    @Inject
    LauncherDirectFactory(@Nonnull final Tty tty,
                          @Nonnull final PrivateDrmLauncherDirectFactory privateDrmLauncherDirectFactory) {
        this.tty = tty;
        this.privateDrmLauncherDirectFactory = privateDrmLauncherDirectFactory;
    }

    public Launcher create() {
        final LauncherDirect drmLauncherDirect = this.privateDrmLauncherDirectFactory.create();
        this.tty.getVtEnterSignal()
                .connect(event -> drmLauncherDirect.getActivateSignal()
                                                   .emit(event));
        this.tty.getVtLeaveSignal()
                .connect(event -> drmLauncherDirect.getDeactivateSignal()
                                                   .emit(event));
        return drmLauncherDirect;
    }
}
