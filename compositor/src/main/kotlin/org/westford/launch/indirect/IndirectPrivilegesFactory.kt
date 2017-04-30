package org.westford.launch.indirect

import javax.inject.Inject

class IndirectPrivilegesFactory @Inject internal constructor(private val privatePrivilegesProxyFactory: PrivatePrivilegesProxyFactory) {

    fun create(): IndirectPrivileges {
        val socketFd1 = Integer.parseInt(System.getenv(NativeConstants.ENV_WESTFORD_LAUNCHER_SOCK))
        return this.privatePrivilegesProxyFactory.create(socketFd1)
    }
}
