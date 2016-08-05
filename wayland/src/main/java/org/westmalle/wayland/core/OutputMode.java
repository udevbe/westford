/*
 * Westmalle Wayland Compositor.
 * Copyright (C) 2016  Erik De Rijcke
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
