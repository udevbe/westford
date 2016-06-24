package org.westmalle.wayland.nativ.libdrm;

import org.freedesktop.jaccall.CType;
import org.freedesktop.jaccall.Field;
import org.freedesktop.jaccall.Struct;

@Struct({
                @Field(name = "clock",
                       type = CType.UNSIGNED_INT),
                @Field(name = "hdisplay",
                       type = CType.UNSIGNED_SHORT),
                @Field(name = "hsync_start",
                       type = CType.UNSIGNED_SHORT),
                @Field(name = "hsync_end",
                       type = CType.UNSIGNED_SHORT),
                @Field(name = "htotal",
                       type = CType.UNSIGNED_SHORT),
                @Field(name = "hskew",
                       type = CType.UNSIGNED_SHORT),
                @Field(name = "vdisplay",
                       type = CType.UNSIGNED_SHORT),
                @Field(name = "vsync_start",
                       type = CType.UNSIGNED_SHORT),
                @Field(name = "vsync_end",
                       type = CType.UNSIGNED_SHORT),
                @Field(name = "vtotal",
                       type = CType.UNSIGNED_SHORT),
                @Field(name = "vskew",
                       type = CType.UNSIGNED_SHORT),
                @Field(name = "vrefresh",
                       type = CType.UNSIGNED_INT),
                @Field(name = "flags",
                       type = CType.UNSIGNED_INT),
                @Field(name = "type",
                       type = CType.UNSIGNED_INT),
                @Field(name = "name",
                       type = CType.CHAR,
                       dataType = String.class,
                       cardinality = Libdrm.DRM_DISPLAY_MODE_LEN)
        })
public final class DrmModeModeInfo extends DrmModeModeInfo_Jaccall_StructType {

}
