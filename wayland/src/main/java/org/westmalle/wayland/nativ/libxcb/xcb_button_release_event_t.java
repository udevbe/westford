package org.westmalle.wayland.nativ.libxcb;


import org.freedesktop.jaccall.CType;
import org.freedesktop.jaccall.Field;
import org.freedesktop.jaccall.Struct;

@Struct({
                @Field(name = "response_type",
                       type = CType.CHAR),
                @Field(name = "detail",
                       type = CType.CHAR),
                @Field(name = "sequence",
                       type = CType.SHORT),
                @Field(name = "time",
                       type = CType.INT),
                @Field(name = "root",
                       type = CType.INT),
                @Field(name = "event",
                       type = CType.INT),
                @Field(name = "child",
                       type = CType.INT),
                @Field(name = "root_x",
                       type = CType.SHORT),
                @Field(name = "root_y",
                       type = CType.SHORT),
                @Field(name = "event_x",
                       type = CType.SHORT),
                @Field(name = "event_y",
                       type = CType.SHORT),
                @Field(name = "state",
                       type = CType.SHORT),
                @Field(name = "same_screen",
                       type = CType.CHAR),
                @Field(name = "pad0",
                       type = CType.CHAR),
        })
public final class xcb_button_release_event_t extends xcb_button_release_event_t_Jaccall_StructType {}
