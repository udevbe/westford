package org.westmalle.wayland.core;

import com.google.auto.value.AutoValue;

import javax.annotation.Nonnegative;

@AutoValue
public abstract class OutputMode {

    public static Builder builder() {
        return new AutoValue_OutputMode.Builder();
    }

    /**
     * @return bitfield of mode flags
     */
    public abstract int getFlags();

    /**
     * @return width of the mode in hardware units
     */
    @Nonnegative
    public abstract int getWidth();

    /**
     * @return height of the mode in hardware units
     */
    @Nonnegative
    public abstract int getHeight();

    /**
     * @return vertical refresh rate in mHz
     */
    @Nonnegative
    public abstract int getRefresh();

    public abstract Builder toBuilder();

    @AutoValue.Builder
    public interface Builder {

        Builder flags(int flags);

        Builder width(@Nonnegative int width);

        Builder height(@Nonnegative int height);

        Builder refresh(@Nonnegative int refresh);

        OutputMode build();
    }
}
