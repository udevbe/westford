package org.westmalle.wayland.nativ;


import com.sun.jna.Native;
import com.sun.jna.Pointer;

public class LibX11xcb {
    public static final int XCBOwnsEventQueue = 1;

    static {
        Native.register("X11-xcb");
    }

    public native Pointer XGetXCBConnection(Pointer dpy);

    public native void XSetEventQueueOwner(Pointer dpy,
                                           int owner);
}
