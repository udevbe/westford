package org.westmalle.wayland.nativ.libxkbcommonx11;


import com.sun.jna.Pointer;

public class Libxkbcommonx11 {

    public native int xkb_x11_get_core_keyboard_device_id(Pointer connection);

    public native Pointer xkb_x11_keymap_new_from_device(Pointer context,
                                                         Pointer connection,
                                                         int device_id,
                                                         int flags);

    public native Pointer xkb_x11_state_new_from_device(Pointer keymap,
                                                        Pointer connection,
                                                        int device_id);
}
