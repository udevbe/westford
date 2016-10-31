package org.westford.nativ.linux;


import org.freedesktop.jaccall.CType;
import org.freedesktop.jaccall.Field;
import org.freedesktop.jaccall.Struct;

@Struct({
                @Field(name = "ssi_signo",
                       type = CType.UNSIGNED_INT),
                @Field(name = "ssi_errno",
                       type = CType.INT),
                @Field(name = "ssi_code",
                       type = CType.INT),
                @Field(name = "ssi_pid",
                       type = CType.UNSIGNED_INT),
                @Field(name = "ssi_uid",
                       type = CType.UNSIGNED_INT),
                @Field(name = "ssi_fd",
                       type = CType.INT),
                @Field(name = "ssi_tid",
                       type = CType.UNSIGNED_INT),
                @Field(name = "ssi_band",
                       type = CType.UNSIGNED_INT),
                @Field(name = "ssi_overrun",
                       type = CType.UNSIGNED_INT),
                @Field(name = "ssi_trapno",
                       type = CType.UNSIGNED_INT),
                @Field(name = "ssi_status",
                       type = CType.INT),
                @Field(name = "ssi_int",
                       type = CType.INT),
                @Field(name = "ssi_ptr",
                       type = CType.UNSIGNED_LONG_LONG),
                @Field(name = "ssi_utime",
                       type = CType.UNSIGNED_LONG_LONG),
                @Field(name = "ssi_stime",
                       type = CType.UNSIGNED_LONG_LONG),
                @Field(name = "ssi_addr",
                       type = CType.UNSIGNED_LONG_LONG),
                @Field(name = "ssi_addr_lsb",
                       type = CType.UNSIGNED_SHORT),
                @Field(name = "__pad",
                       cardinality = 46,
                       type = CType.UNSIGNED_CHAR),
        })
public final class signalfd_siginfo extends signalfd_siginfo_Jaccall_StructType {}
