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
import javax.annotation.Nonnull;

@AutoValue
public abstract class OutputGeometry {

    public static Builder builder() {
        return new AutoValue_OutputGeometry.Builder();
    }

    /**
     * @return x position within the global compositor space
     */
    public abstract int getX();

    /**
     * @return y position within the global compositor space
     */
    public abstract int getY();

    /**
     * @return width in millimeters of the output
     */
    @Nonnegative
    public abstract int getPhysicalWidth();

    /**
     * @return height in millimeters of the output
     */
    @Nonnegative
    public abstract int getPhysicalHeight();

    /**
     * @return subpixel orientation of the output
     */
    public abstract int getSubpixel();

    /**
     * @return textual description of the manufacturer
     */
    @Nonnull
    public abstract String getMake();

    /**
     * @return textual description of the model
     */
    @Nonnull
    public abstract String getModel();

    /**
     * @return transform that maps framebuffer to output
     */
    public abstract int getTransform();

    public abstract Builder toBuilder();

    @AutoValue.Builder
    public interface Builder {
        Builder x(int x);

        Builder y(int y);

        Builder physicalWidth(@Nonnegative int width);

        Builder physicalHeight(@Nonnegative int height);

        Builder subpixel(int subpixel);

        Builder make(String make);

        Builder model(String model);

        Builder transform(int transform);

        OutputGeometry build();
    }
}
