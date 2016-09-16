package org.westmalle.launch.indirect;

import org.freedesktop.jaccall.CType;
import org.freedesktop.jaccall.Field;
import org.freedesktop.jaccall.Struct;

@Struct(value = {
        @Field(name = "flags",
               type = CType.INT),
        @Field(name = "path",
               cardinality = 0,
               //variable array. total size of this struct is stored in the privilege_req struct that should wrap this struct.
               type = CType.CHAR,
               dataType = String.class)
})
public final class privilege_req_open extends privilege_req_open_Jaccall_StructType {
}
