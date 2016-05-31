package org.westmalle.wayland.nativ.libinput;

import org.freedesktop.jaccall.CType;
import org.freedesktop.jaccall.Field;
import org.freedesktop.jaccall.Struct;

@Struct({
        @Field(name = "open_restricted",
                type = CType.POINTER,
                dataType = open_restricted.class),
        @Field(name = "close_restricted",
                type = CType.POINTER,
                dataType = close_restricted.class),
})
public final class libinput_interface extends libinput_interface_Jaccall_StructType {
}
