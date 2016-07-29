package org.westmalle.wayland.tty;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class VtLeave {
    public static VtLeave create() {
        return new AutoValue_VtLeave();
    }
}
