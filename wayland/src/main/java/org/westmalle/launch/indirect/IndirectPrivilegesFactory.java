package org.westmalle.launch.indirect;

import javax.annotation.Nonnull;
import javax.inject.Inject;

public class IndirectPrivilegesFactory {

    @Nonnull
    private final PrivatePrivilegesProxyFactory privatePrivilegesProxyFactory;

    @Inject
    IndirectPrivilegesFactory(@Nonnull final PrivatePrivilegesProxyFactory privatePrivilegesProxyFactory) {
        this.privatePrivilegesProxyFactory = privatePrivilegesProxyFactory;
    }

    public IndirectPrivileges create() {
        final int socketFd1 = Integer.parseInt(System.getenv(IndirectLauncher.SOCKETFD_1));
        return this.privatePrivilegesProxyFactory.create(socketFd1);
    }
}
