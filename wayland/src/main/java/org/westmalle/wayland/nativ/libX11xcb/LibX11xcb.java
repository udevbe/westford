package org.westmalle.wayland.nativ.libX11xcb;


import com.sun.jna.Pointer;

public class LibX11xcb {
    public static final int XCBOwnsEventQueue = 1;

    public native Pointer XGetXCBConnection(Pointer dpy);

    public native void XSetEventQueueOwner(Pointer dpy,
                                           int owner);
}
