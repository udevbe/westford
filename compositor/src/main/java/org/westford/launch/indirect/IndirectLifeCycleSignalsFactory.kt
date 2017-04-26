package org.westford.launch.indirect


import org.freedesktop.wayland.server.Display
import javax.inject.Inject

import org.freedesktop.wayland.server.jaccall.WaylandServerCore.WL_EVENT_READABLE

class IndirectLifeCycleSignalsFactory @Inject
internal constructor(private val privateIndirectLifeCycleSignalsFactory: PrivateIndirectLifeCycleSignalsFactory,
                     private val display: Display) {

    fun create(): IndirectLifeCycleSignals {
        val socketFd1 = Integer.parseInt(System.getenv(NativeConstants.ENV_WESTFORD_LAUNCHER_SOCK))

        val indirectLifeCycleSignals = this.privateIndirectLifeCycleSignalsFactory.create(socketFd1)
        this.display.eventLoop
                .addFileDescriptor(socketFd1,
                        WL_EVENT_READABLE,
                        FileDescriptorEventHandler { fd, mask -> indirectLifeCycleSignals.handleLauncherEvent(fd, mask) })

        return indirectLifeCycleSignals
    }
}
