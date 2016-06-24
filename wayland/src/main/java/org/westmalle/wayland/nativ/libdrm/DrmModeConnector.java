package org.westmalle.wayland.nativ.libdrm;

import org.freedesktop.jaccall.CType;
import org.freedesktop.jaccall.Field;
import org.freedesktop.jaccall.Struct;

@Struct({
                @Field(name = "connector_id",
                       type = CType.UNSIGNED_INT),
                @Field(name = "encoder_id",
                       type = CType.UNSIGNED_INT),
                @Field(name = "connector_type",
                       type = CType.UNSIGNED_INT),
                @Field(name = "connector_type_id",
                       type = CType.UNSIGNED_INT),
                @Field(name = "connection",
                       type = CType.INT),
                @Field(name = "mmWidth",
                       type = CType.UNSIGNED_INT),
                @Field(name = "mmHeight",
                       type = CType.UNSIGNED_INT),
                @Field(name = "drmModeSubPixel",
                       type = CType.INT),
                @Field(name = "count_modes",
                       type = CType.INT),
                @Field(name = "modes",
                       type = CType.POINTER,
                       dataType = DrmModeModeInfo.class),
                @Field(name = "count_props",
                       type = CType.INT),
                @Field(name = "props",
                       type = CType.POINTER,
                       dataType = Integer.class),
                @Field(name = "prop_values",
                       type = CType.POINTER,
                       dataType = Long.class),
                @Field(name = "count_encoders",
                       type = CType.INT),
                @Field(name = "encoders",
                       type = CType.UNSIGNED_INT),

        })
public final class DrmModeConnector extends DrmModeConnector_Jaccall_StructType {}
