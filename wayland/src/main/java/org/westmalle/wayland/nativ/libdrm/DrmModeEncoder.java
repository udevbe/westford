package org.westmalle.wayland.nativ.libdrm;

import org.freedesktop.jaccall.CType;
import org.freedesktop.jaccall.Field;
import org.freedesktop.jaccall.Struct;

@Struct({
                @Field(name = "encoder_id",
                       type = CType.UNSIGNED_INT),
                @Field(name = "encoder_type",
                       type = CType.UNSIGNED_INT),
                @Field(name = "crtc_id",
                       type = CType.UNSIGNED_INT),
                @Field(name = "possible_crtcs",
                       type = CType.UNSIGNED_INT),
                @Field(name = "possible_clones",
                       type = CType.UNSIGNED_INT),
        })
public final class DrmModeEncoder extends DrmModeEncoder_Jaccall_StructType {}
