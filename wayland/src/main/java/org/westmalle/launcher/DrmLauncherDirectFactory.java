package org.westmalle.launcher;


import org.westmalle.tty.Tty;

import javax.annotation.Nonnull;
import javax.inject.Inject;

public class DrmLauncherDirectFactory {

    @Nonnull
    private final Tty                             tty;
    @Nonnull
    private final PrivateDrmLauncherDirectFactory privateDrmLauncherDirectFactory;

    @Inject
    DrmLauncherDirectFactory(@Nonnull final Tty tty,
                             @Nonnull final PrivateDrmLauncherDirectFactory privateDrmLauncherDirectFactory) {
        this.tty = tty;
        this.privateDrmLauncherDirectFactory = privateDrmLauncherDirectFactory;
    }

    public DrmLauncher create() {
        final DrmLauncherDirect drmLauncherDirect = this.privateDrmLauncherDirectFactory.create();
        this.tty.getVtEnterSignal()
                .connect(event -> drmLauncherDirect.getActivateSignal()
                                                   .emit(event));
        this.tty.getVtLeaveSignal()
                .connect(event -> drmLauncherDirect.getDeactivateSignal()
                                                   .emit(event));
        return drmLauncherDirect;
    }
}
