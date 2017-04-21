package org.westford.nativ.libdrm;

import org.freedesktop.jaccall.CType;
import org.freedesktop.jaccall.Field;
import org.freedesktop.jaccall.Struct;

@Struct(value = {
        @Field(name = "count_planes",
               type = CType.UNSIGNED_INT),
        @Field(name = "planes",
               type = CType.POINTER,
               dataType = Integer.class)
})
public final class drmModePlaneRes extends drmModePlaneRes_Jaccall_StructType {}
