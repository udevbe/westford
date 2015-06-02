package org.westmalle.wayland.nativ;


import com.sun.jna.Native;
import com.sun.jna.Pointer;

public class LibX11xcb {
    static {
        Native.register("X11-xcb");
    }

    public static final int XlibOwnsEventQueue = 0;
    public static final int XCBOwnsEventQueue = 1;

    public native Pointer XGetXCBConnection(Pointer dpy);

    public native void XSetEventQueueOwner(Pointer dpy, int owner);
}
