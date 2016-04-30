package org.westmalle.wayland.nativ.libxcb;

import org.freedesktop.jaccall.CType;
import org.freedesktop.jaccall.Field;
import org.freedesktop.jaccall.Struct;

@Struct({
                @Field(name = "response_type",
                       type = CType.CHAR),
                @Field(name = "keys",
                       type = CType.CHAR,
                       cardinality = 31)
        })
public final class xcb_keymap_notify_event_t extends xcb_keymap_notify_event_t_Jaccall_StructType {}
