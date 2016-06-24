package org.westmalle.wayland.nativ.libdrm;

import org.freedesktop.jaccall.Functor;
import org.freedesktop.jaccall.Ptr;
import org.freedesktop.jaccall.Unsigned;

@Functor
@FunctionalInterface
public interface vblank_handler {
    void $(int fd,
           @Unsigned int sequence,
           @Unsigned int tv_sec,
           @Unsigned int tv_usec,
           @Ptr long user_data);
}
