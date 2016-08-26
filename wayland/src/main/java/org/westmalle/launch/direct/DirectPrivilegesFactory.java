package org.westmalle.launch.direct;


import org.westmalle.launch.Privileges;
import org.westmalle.tty.Tty;

import javax.annotation.Nonnull;
import javax.inject.Inject;

public class DirectPrivilegesFactory {

    @Nonnull
    private final Tty                            tty;
    @Nonnull
    private final PrivateDirectPrivilegesFactory privateDirectPrivilegesFactory;

    @Inject
    DirectPrivilegesFactory(@Nonnull final Tty tty,
                            @Nonnull final PrivateDirectPrivilegesFactory privateDirectPrivilegesFactory) {
        this.tty = tty;
        this.privateDirectPrivilegesFactory = privateDirectPrivilegesFactory;
    }

    public Privileges create() {
        final DirectPrivileges directPrivileges = this.privateDirectPrivilegesFactory.create();
        this.tty.getVtEnterSignal()
                .connect(event -> directPrivileges.getActivateSignal()
                                                  .emit(event));
        this.tty.getVtLeaveSignal()
                .connect(event -> directPrivileges.getDeactivateSignal()
                                                  .emit(event));
        return directPrivileges;
    }
}
