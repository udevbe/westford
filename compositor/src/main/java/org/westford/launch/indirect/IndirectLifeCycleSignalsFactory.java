package org.westford.launch.indirect;


import org.freedesktop.wayland.server.Display;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import static org.freedesktop.wayland.server.jaccall.WaylandServerCore.WL_EVENT_READABLE;

public class IndirectLifeCycleSignalsFactory {

    @Nonnull
    private final PrivateIndirectLifeCycleSignalsFactory privateIndirectLifeCycleSignalsFactory;
    @Nonnull
    private final Display                                display;

    @Inject
    IndirectLifeCycleSignalsFactory(@Nonnull final PrivateIndirectLifeCycleSignalsFactory privateIndirectLifeCycleSignalsFactory,
                                    @Nonnull final Display display) {
        this.privateIndirectLifeCycleSignalsFactory = privateIndirectLifeCycleSignalsFactory;
        this.display = display;
    }

    public IndirectLifeCycleSignals create() {
        final int socketFd1 = Integer.parseInt(System.getenv(NativeConstants.ENV_WESTFORD_LAUNCHER_SOCK));

        final IndirectLifeCycleSignals indirectLifeCycleSignals = this.privateIndirectLifeCycleSignalsFactory.create(socketFd1);
        this.display.getEventLoop()
                    .addFileDescriptor(socketFd1,
                                       WL_EVENT_READABLE,
                                       indirectLifeCycleSignals::handleLauncherEvent);

        return indirectLifeCycleSignals;
    }
}
