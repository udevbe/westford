package org.westmalle.wayland.nativ.libbcm_host;

import com.github.zubnix.jaccall.CType;
import com.github.zubnix.jaccall.Field;
import com.github.zubnix.jaccall.Struct;

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
