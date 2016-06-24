package org.westmalle.wayland.nativ.libdrm;

import org.freedesktop.jaccall.CType;
import org.freedesktop.jaccall.Field;
import org.freedesktop.jaccall.Struct;

@Struct({
                @Field(name = "count_fbs",
                       type = CType.INT),
                @Field(name = "fbs",
                       type = CType.POINTER,
                       dataType = Integer.class),
                @Field(name = "count_crtcs",
                       type = CType.INT),
                @Field(name = "crtcs",
                       type = CType.POINTER,
                       dataType = Integer.class),
                @Field(name = "count_connectors",
                       type = CType.INT),
                @Field(name = "connectors",
                       type = CType.POINTER,
                       dataType = Integer.class),
                @Field(name = "count_encoders",
                       type = CType.INT),
                @Field(name = "encoders",
                       type = CType.POINTER,
                       dataType = Integer.class),
                @Field(name = "min_width",
                       type = CType.UNSIGNED_INT),
                @Field(name = "max_width",
                       type = CType.UNSIGNED_INT),
                @Field(name = "min_height",
                       type = CType.UNSIGNED_INT),
                @Field(name = "max_height",
                       type = CType.UNSIGNED_INT),
        })
public final class DrmModeRes extends DrmModeRes_Jaccall_StructType {}
