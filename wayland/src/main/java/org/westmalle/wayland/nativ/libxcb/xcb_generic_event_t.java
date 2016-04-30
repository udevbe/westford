package org.westmalle.wayland.nativ.libxcb;

import org.freedesktop.jaccall.CType;
import org.freedesktop.jaccall.Field;
import org.freedesktop.jaccall.Struct;

@Struct({
                @Field(name = "response_type",
                       type = CType.CHAR),
                @Field(name = "pad0",
                       type = CType.CHAR),
                @Field(name = "sequence",
                       type = CType.SHORT),
                @Field(name = "pad",
                       type = CType.INT,
                       cardinality = 7),
                @Field(name = "full_sequence",
                       type = CType.INT),

        })
public final class xcb_generic_event_t extends xcb_generic_event_t_Jaccall_StructType {}
