package org.westmalle.nativ.glibc;

import org.freedesktop.jaccall.CType;
import org.freedesktop.jaccall.Field;
import org.freedesktop.jaccall.Struct;

@Struct(value = {
        @Field(name = "iov_base",
               type = CType.POINTER,
               dataType = Void.class),
        @Field(name = "iov_len",
               type = CType.UNSIGNED_LONG)
})
public final class iovec extends iovec_Jaccall_StructType {}
