package org.westmalle.wayland.x11;


import org.freedesktop.wayland.server.EventLoop;
import org.westmalle.wayland.nativ.Libxcb;
import org.westmalle.wayland.nativ.xcb_generic_event_t;

public class X11Seat implements EventLoop.FileDescriptorEventHandler {

    private final Libxcb libxcb;
    private final X11OutputImplementation x11OutputImplementation;

    X11Seat(final Libxcb libxcb,
            final X11OutputImplementation x11OutputImplementation) {
        this.libxcb = libxcb;
        this.x11OutputImplementation = x11OutputImplementation;
    }

    @Override
    public int handle(final int fd,
                      final int mask) {
        final xcb_generic_event_t xcbEvent = this.libxcb.xcb_poll_for_event(this.x11OutputImplementation.getXcbConnection());

        return 0;
    }
}
