package org.westmalle.wayland.nativ.glibc;

import org.freedesktop.jaccall.Lib;
import org.freedesktop.jaccall.Ptr;

@Lib(value = "pthread",
     version = 0)
public class Libpthread {
    public native int pthread_sigmask(int how,
                                      @Ptr long set,
                                      @Ptr long oldset);

    public native int sigemptyset(@Ptr long set);

    public native int sigaddset(@Ptr long set,
                                int signo);
}
