package org.westmalle.wayland.x11;

import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.EventLoop;
import org.westmalle.wayland.nativ.LibX11;
import org.westmalle.wayland.nativ.Libxcb;
import org.westmalle.wayland.output.KeyboardFactory;
import org.westmalle.wayland.output.PointerDeviceFactory;
import org.westmalle.wayland.protocol.WlKeyboardFactory;
import org.westmalle.wayland.protocol.WlPointerFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;

public class X11SeatFactory {

    @Nonnull
    private final Display display;
    @Nonnull
    private final LibX11 libX11;
    @Nonnull
    private final Libxcb libxcb;
    @Nonnull
    private final WlPointerFactory wlPointerFactory;
    @Nonnull
    private final WlKeyboardFactory wlKeyboardFactory;
    @Nonnull
    private final PointerDeviceFactory pointerDeviceFactory;
    @Nonnull
    private final KeyboardFactory keyboardFactory;

    @Inject
    X11SeatFactory(@Nonnull final Display display,
                   @Nonnull final LibX11 libX11,
                   @Nonnull final Libxcb libxcb,
                   @Nonnull final WlPointerFactory wlPointerFactory,
                   @Nonnull final WlKeyboardFactory wlKeyboardFactory,
                   @Nonnull final PointerDeviceFactory pointerDeviceFactory,
                   @Nonnull final KeyboardFactory keyboardFactory) {
        this.display = display;
        this.libX11 = libX11;
        this.libxcb = libxcb;

        this.wlPointerFactory = wlPointerFactory;
        this.wlKeyboardFactory = wlKeyboardFactory;
        this.pointerDeviceFactory = pointerDeviceFactory;
        this.keyboardFactory = keyboardFactory;
    }

    public X11Seat create(X11OutputImplementation x11OutputImplementation) {
        final int xDisplayFD = this.libX11.XConnectionNumber(x11OutputImplementation.getxDisplay());
        final X11Seat x11Seat = new X11Seat(this.libxcb,
                                            x11OutputImplementation);
        this.display.getEventLoop().addFileDescriptor(xDisplayFD,
                                                      EventLoop.EVENT_READABLE,
                                                      x11Seat);
        return x11Seat;
    }
}
