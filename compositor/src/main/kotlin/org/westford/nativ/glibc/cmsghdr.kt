package org.westford.nativ.glibc

import org.freedesktop.jaccall.CType
import org.freedesktop.jaccall.Field
import org.freedesktop.jaccall.Struct

@Struct(Field(name = "cmsg_len",
              type = CType.UNSIGNED_LONG),
        Field(name = "cmsg_level",
              type = CType.INT),
        Field(name = "cmsg_type",
              type = CType.INT),
        Field(name = "__cmsg_data",
              cardinality = 0,
              type = CType.UNSIGNED_CHAR)) class cmsghdr : cmsghdr_Jaccall_StructType()
