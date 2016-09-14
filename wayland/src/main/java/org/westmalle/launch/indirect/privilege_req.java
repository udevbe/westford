package org.westmalle.launch.indirect;

import org.freedesktop.jaccall.CType;
import org.freedesktop.jaccall.Field;
import org.freedesktop.jaccall.Struct;

@Struct(value = {@Field(name = "opcode",
                        type = CType.INT),
                 @Field(name = "payload_size",
                        type = CType.UNSIGNED_LONG),
                 @Field(name = "payload",
                        cardinality = 0,
                        type = CType.UNSIGNED_CHAR)})
public final class privilege_req extends privilege_req_Jaccall_StructType {}
