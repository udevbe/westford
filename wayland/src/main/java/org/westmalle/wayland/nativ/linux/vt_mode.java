package org.westmalle.wayland.nativ.linux;


import org.freedesktop.jaccall.CType;
import org.freedesktop.jaccall.Field;
import org.freedesktop.jaccall.Struct;

@Struct({
                @Field(name = "mode",
                       type = CType.CHAR),
                @Field(name = "waitv",
                       type = CType.CHAR),
                @Field(name = "relsig",
                       type = CType.CHAR),
                @Field(name = "acqsig",
                       type = CType.CHAR),
                @Field(name = "frsig",
                       type = CType.CHAR),
        })
public final class vt_mode extends vt_mode_Jaccall_StructType {}
