package org.westmalle.wayland.nativ.libinput;

import org.freedesktop.jaccall.Lib;
import org.freedesktop.jaccall.Ptr;

import javax.inject.Singleton;

@Singleton
@Lib("input")
public class Libinput {

    @Ptr
    public native long libinput_udev_create_context(@Ptr(libinput_interface.class) long interface_,
                                                    @Ptr(Void.class) long user_data,
                                                    @Ptr long udev);

    public native int libinput_udev_assign_seat(@Ptr long libinput,
                                                @Ptr(String.class) long seat_id);

    public native void libinput_log_set_handler(@Ptr long libinput, @Ptr(libinput_log_handler.class) long log_handler);

    public native void libinput_log_set_priority(@Ptr long libinput, int priority);

    public native int libinput_dispatch(@Ptr long libinput);

    @Ptr
    public native long libinput_get_event(@Ptr long libinput);

    @Ptr
    public native long libinput_event_destroy(@Ptr long libinput);

    public native int libinput_get_fd(@Ptr long libinput);
}
