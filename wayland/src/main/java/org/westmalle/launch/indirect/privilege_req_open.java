package org.westmalle.launch.indirect;

import org.freedesktop.jaccall.CType;
import org.freedesktop.jaccall.Field;
import org.freedesktop.jaccall.Struct;

@Struct(value = {
        @Field(name = "path",
               type = CType.UNSIGNED_CHAR),
        @Field(name = "flags",
               type = CType.INT)
})
public final class privilege_req_open extends privilege_req_open_Jaccall_StructType {
}
