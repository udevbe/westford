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
public abstract class Rectangle {

    public static final Rectangle ZERO = builder().build();

    public static Rectangle create(final int x,
                                   final int y,
                                   @Nonnegative final int width,
                                   @Nonnegative final int height) {
        return builder().x(x)
                        .y(y)
                        .width(width)
                        .height(height)
                        .build();
    }

    public static Builder builder() {
        return new AutoValue_Rectangle.Builder().x(0)
                                                .y(0)
                                                .width(0)
                                                .height(0);
    }

    @Nonnegative
    public abstract int getWidth();

    @Nonnegative
    public abstract int getHeight();

    @Nonnull
    public Point getPosition() {
        return Point.create(getX(),
                            getY());
    }

    public abstract int getX();

    public abstract int getY();

    public abstract Builder toBuilder();

    @AutoValue.Builder
    public interface Builder {
        Builder x(int x);

        Builder y(int y);

        Builder width(@Nonnegative int width);

        Builder height(@Nonnegative int height);

        Rectangle build();
    }
}
