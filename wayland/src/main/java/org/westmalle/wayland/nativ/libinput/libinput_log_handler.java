package org.westmalle.wayland.nativ.libinput;

import org.freedesktop.jaccall.Functor;
import org.freedesktop.jaccall.Ptr;

@Functor
@FunctionalInterface
public interface libinput_log_handler {

    //TODO at va_list (args) support to jaccall
    void $(@Ptr long libinput, int priority, @Ptr(String.class) long format, @Ptr long va_list_args);
}
