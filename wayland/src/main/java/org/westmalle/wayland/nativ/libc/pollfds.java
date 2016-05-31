package org.westmalle.wayland.nativ.libc;

import org.freedesktop.jaccall.CType;
import org.freedesktop.jaccall.Field;
import org.freedesktop.jaccall.Struct;

@Struct({
        @Field(name = "fd", type = CType.INT),
        @Field(name = "events",type = CType.SHORT),
        @Field(name = "revents", type=CType.SHORT)
})
public final class pollfds extends pollfds_Jaccall_StructType {}
