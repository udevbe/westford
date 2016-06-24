package org.westmalle.wayland.nativ.libdrm;

import org.freedesktop.jaccall.CType;
import org.freedesktop.jaccall.Field;
import org.freedesktop.jaccall.Struct;

@Struct({
                @Field(name = "version",
                       type = CType.INT),
                @Field(name = "vblank_handler",
                       type = CType.POINTER,
                       dataType = vblank_handler.class),
                @Field(name = "page_flip_handler",
                       type = CType.POINTER,
                       dataType = page_flip_handler.class)
        })
public final class DrmEventContext extends DrmEventContext_Jaccall_StructType {}
