package org.westmalle.wayland.tty;


import com.google.auto.value.AutoValue;

@AutoValue
public abstract class VtEnter {
    public static VtEnter create() {
        return new AutoValue_VtEnter();
    }
}
