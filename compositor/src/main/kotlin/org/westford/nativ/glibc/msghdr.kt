package org.westford.nativ.glibc

import org.freedesktop.jaccall.CType
import org.freedesktop.jaccall.Field
import org.freedesktop.jaccall.Struct

@Struct(Field(name = "msg_name",
              type = CType.POINTER,
              dataType = Void::class),
        Field(name = "msg_namelen",
              type = CType.UNSIGNED_LONG),
        Field(name = "msg_iov",
              type = CType.POINTER,
              dataType = iovec::class),
        Field(name = "msg_iovlen",
              type = CType.UNSIGNED_LONG),
        Field(name = "msg_control",
              type = CType.POINTER,
              dataType = Void::class),
        Field(name = "msg_controllen",
              type = CType.UNSIGNED_LONG),
        Field(name = "msg_flags",
              type = CType.INT)) class msghdr : msghdr_Jaccall_StructType()
