package org.westmalle.launch.direct;

import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.EventLoop;
import org.westmalle.launch.Launcher;
import org.westmalle.tty.Tty;
import org.westmalle.wayland.core.LifeCycleSignals;

import javax.annotation.Nonnull;
import javax.inject.Inject;


public class DirectLauncher implements Launcher {


    @Nonnull
    private final Display          display;
    @Nonnull
    private final LifeCycleSignals lifeCycleSignals;
    @Nonnull
    private final Tty              tty;

    @Inject
    DirectLauncher(@Nonnull final Display display,
                   @Nonnull final LifeCycleSignals lifeCycleSignals,
                   @Nonnull final Tty tty) {
        this.display = display;
        this.lifeCycleSignals = lifeCycleSignals;
        this.tty = tty;
    }

    @Override
    public void launch(final Class<?> main,
                       final String[] args) throws Exception {

        this.tty.getVtEnterSignal()
                .connect(event -> this.lifeCycleSignals.getActivateSignal()
                                                       .emit(event));
        this.tty.getVtLeaveSignal()
                .connect(event -> this.lifeCycleSignals.getDeactivateSignal()
                                                       .emit(event));

        final short relSig = this.tty.getRelSig();
        final short acqSig = this.tty.getAcqSig();

        final EventLoop eventLoop = this.display.getEventLoop();
        eventLoop.addSignal(relSig,
                            signalNumber -> {
                                this.tty.handleVtLeave();
                                return 0;
                            });
        eventLoop.addSignal(acqSig,
                            signalNumber -> {
                                this.tty.handleVtEnter();
                                return 0;
                            });

        main.getMethod("main",
                       String[].class)
            .invoke(null,
                    (Object) args);
    }
}
