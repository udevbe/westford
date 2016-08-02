package org.westmalle.wayland.nativ.glibc;


import org.freedesktop.jaccall.CType;
import org.freedesktop.jaccall.Field;
import org.freedesktop.jaccall.Struct;

@Struct({
                @Field(name = "c_iflag",
                       type = CType.UNSIGNED_INT),
                @Field(name = "c_oflag",
                       type = CType.UNSIGNED_INT),
                @Field(name = "c_cflag",
                       type = CType.UNSIGNED_INT),
                @Field(name = "c_lflag",
                       type = CType.UNSIGNED_INT),
                @Field(name = "c_line",
                       type = CType.UNSIGNED_CHAR),
                @Field(name = "c_cc",
                       type = CType.UNSIGNED_CHAR,
                       cardinality = Libc.NCCS),
                @Field(name = "c_ispeed",
                       type = CType.UNSIGNED_INT),
                @Field(name = "c_ospeed",
                       type = CType.UNSIGNED_INT),
        })
public final class termios extends termios_Jaccall_StructType {}
