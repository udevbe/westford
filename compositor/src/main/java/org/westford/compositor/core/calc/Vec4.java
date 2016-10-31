/*
 * Westford Wayland Compositor.
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
package org.westford.compositor.core.calc;

import com.google.auto.value.AutoValue;
import org.westford.compositor.core.Point;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

@AutoValue
public abstract class Vec4 {

    public static Vec4 create(@Nonnull final float[] array) {
        return Vec4.create(array,
                           0);
    }

    public static Vec4 create(@Nonnull final float[] array,
                              @Nonnegative final int offset) {
        if (array.length < 4) {
            throw new IllegalArgumentException("Array length must be >= 4");
        }
        return Vec4.builder()
                   .x(array[offset])
                   .y(array[1 + offset])
                   .z(array[2 + offset])
                   .w(array[3 + offset])
                   .build();
    }

    public static Builder builder() {
        return new AutoValue_Vec4.Builder().x(0)
                                           .y(0)
                                           .z(0)
                                           .w(0);
    }

    public abstract Builder toBuilder();

    public Vec4 add(@Nonnull final Vec4 right) {
        return Vec4.create(getX() + right.getX(),
                           getY() + right.getY(),
                           getZ() + right.getZ(),
                           getW() + right.getW());
    }

    public static Vec4 create(final float x,
                              final float y,
                              final float z,
                              final float w) {
        return Vec4.builder()
                   .x(x)
                   .y(y)
                   .z(z)
                   .w(w)
                   .build();
    }

    public abstract float getX();

    public abstract float getY();

    public abstract float getZ();

    public abstract float getW();

    public Vec4 subtract(@Nonnull final Vec4 right) {
        return Vec4.create(getX() - right.getX(),
                           getY() - right.getY(),
                           getZ() - right.getZ(),
                           getW() - right.getW());
    }

    public Point toPoint() {
        return Point.create((int) getX(),
                            (int) getY());
    }

    @AutoValue.Builder
    public interface Builder {

        Builder x(float x);

        Builder y(float y);

        Builder z(float z);

        Builder w(float z);

        Vec4 build();
    }
}
