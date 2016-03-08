package org.westmalle.wayland.nativ.libxcb;

import com.github.zubnix.jaccall.CType;
import com.github.zubnix.jaccall.Field;
import com.github.zubnix.jaccall.Struct;

@Struct({
                @Field(name = "response_type",
                       type = CType.UNSIGNED_CHAR),
                @Field(name = "pad0",
                       type = CType.UNSIGNED_CHAR),
                @Field(name = "sequence",
                       type = CType.UNSIGNED_SHORT),
                @Field(name = "request",
                       type = CType.UNSIGNED_CHAR),
                @Field(name = "first_keycode",
                       type = CType.UNSIGNED_CHAR),
                @Field(name = "count",
                       type = CType.UNSIGNED_CHAR),
                @Field(name = "pad1",
                       type = CType.UNSIGNED_CHAR)
        })
public final class xcb_mapping_notify_event_t extends xcb_mapping_notify_event_t_Jaccall_StructType{}
