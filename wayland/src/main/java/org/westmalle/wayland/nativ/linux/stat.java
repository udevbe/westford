package org.westmalle.wayland.nativ.linux;

import org.freedesktop.jaccall.CType;
import org.freedesktop.jaccall.Field;
import org.freedesktop.jaccall.Struct;

@Struct({
                @Field(name = "st_dev",
                       type = CType.UNSIGNED_INT),
                @Field(name = "st_ino",
                       type = CType.UNSIGNED_LONG),
                @Field(name = "st_mode",
                       type = CType.UNSIGNED_INT),
                @Field(name = "st_nlink",
                       type = CType.UNSIGNED_INT),
                @Field(name = "st_uid",
                       type = CType.UNSIGNED_INT),
                @Field(name = "st_gid",
                       type = CType.UNSIGNED_INT),
                @Field(name = "st_rdev",
                       type = CType.UNSIGNED_INT),
                @Field(name = "st_size",
                       type = CType.LONG),
                @Field(name = "st_blksize",
                       type = CType.LONG),
                @Field(name = "blkcnt_t",
                       type = CType.UNSIGNED_LONG),
                @Field(name = "st_atime",
                       type = CType.LONG),
                @Field(name = "st_mtime",
                       type = CType.LONG),
                @Field(name = "st_ctime",
                       type = CType.LONG),

        })
public final class stat extends stat_Jaccall_StructType {}
