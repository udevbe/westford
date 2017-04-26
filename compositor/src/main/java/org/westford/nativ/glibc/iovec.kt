package org.westford.nativ.glibc

import org.freedesktop.jaccall.CType
import org.freedesktop.jaccall.Field
import org.freedesktop.jaccall.Struct

@Struct(value = *arrayOf(Field(name = "iov_base", type = CType.POINTER, dataType = Void::class), Field(name = "iov_len", type = CType.UNSIGNED_LONG)))
class iovec : iovec_Jaccall_StructType()
