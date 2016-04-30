package org.westmalle.wayland.nativ.libbcm_host;

import org.freedesktop.jaccall.CType;
import org.freedesktop.jaccall.Field;
import org.freedesktop.jaccall.Struct;

@Struct({
                @Field(name = "width",
                       type = CType.INT),
                @Field(name = "height",
                       type = CType.INT),
                @Field(name = "transform",
                       type = CType.INT),
                @Field(name = "input_format",
                       type = CType.INT),
                @Field(name = "display_num",
                       type = CType.INT),
        })
public final class DISPMANX_MODEINFO_T extends DISPMANX_MODEINFO_T_Jaccall_StructType {}
