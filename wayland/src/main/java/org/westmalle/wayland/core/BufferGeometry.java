package org.westmalle.wayland.core;

import com.google.auto.value.AutoValue;

import javax.annotation.Nonnegative;

@AutoValue
public abstract class BufferGeometry {

    public static BufferGeometry create(@Nonnegative final int width,
                                        @Nonnegative final int height) {
        return new AutoValue_BufferGeometry(width,
                                            height);
    }

    public abstract int getWidth();

    public abstract int getHeight();

}
