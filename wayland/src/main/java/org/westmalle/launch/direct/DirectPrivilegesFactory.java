package org.westmalle.launch.direct;


import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.EventLoop;
import org.westmalle.launch.Privileges;
import org.westmalle.tty.Tty;

import javax.annotation.Nonnull;
import javax.inject.Inject;

public class DirectPrivilegesFactory {

    @Nonnull
    private final Tty                            tty;
    @Nonnull
    private final PrivateDirectPrivilegesFactory privateDirectPrivilegesFactory;
    @Nonnull
    private final Display                        display;

    @Inject
    DirectPrivilegesFactory(@Nonnull final Tty tty,
                            @Nonnull final PrivateDirectPrivilegesFactory privateDirectPrivilegesFactory,
                            @Nonnull final Display display) {
        this.tty = tty;
        this.privateDirectPrivilegesFactory = privateDirectPrivilegesFactory;
        this.display = display;
    }

    public Privileges create() {
        final DirectPrivileges directPrivileges = this.privateDirectPrivilegesFactory.create();
        this.tty.getVtEnterSignal()
                .connect(event -> directPrivileges.getActivateSignal()
                                                  .emit(event));
        this.tty.getVtLeaveSignal()
                .connect(event -> directPrivileges.getDeactivateSignal()
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

        return directPrivileges;
    }
}
