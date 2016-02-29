package org.westmalle.wayland.nativ.libxcb;

import com.github.zubnix.jaccall.CType;
import com.github.zubnix.jaccall.Field;
import com.github.zubnix.jaccall.Struct;

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
