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

        final int socketFd0 = Integer.parseInt(System.getProperty(IndirectLauncher.SOCKETFD_0));
        final int socketFd1 = Integer.parseInt(System.getProperty(IndirectLauncher.SOCKETFD_0));

        return this.privatePrivilegesProxyFactory.create(socketFd0,
                                                         socketFd1);
    }
}
