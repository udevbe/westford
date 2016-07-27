package org.westmalle.wayland.nativ.linux;

import org.freedesktop.jaccall.CType;
import org.freedesktop.jaccall.Field;
import org.freedesktop.jaccall.Struct;


@Struct({
                @Field(name = "v_active",
                       type = CType.UNSIGNED_SHORT),
                @Field(name = "v_signal",
                       type = CType.UNSIGNED_SHORT),
                @Field(name = "v_state",
                       type = CType.UNSIGNED_SHORT),
        })
public final class vt_stat {
}
