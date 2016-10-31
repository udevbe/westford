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
package org.westford.compositor.core;

import com.google.auto.value.AutoValue;
import org.westford.compositor.core.calc.Vec4;

@AutoValue
public abstract class Point {

    public static final Point ZERO = builder().build();

    public Vec4 toVec4() {
        return Vec4.create(getX(),
                           getY(),
                           0,
                           1);
    }

    public abstract int getX();

    public abstract int getY();

    public Point add(final Point right) {
        return Point.create(getX() + right.getX(),
                            getY() + right.getY());
    }

    public static Point create(final int x,
                               final int y) {
        return builder().x(x)
                        .y(y)
                        .build();
    }

    public static Builder builder() {
        return new AutoValue_Point.Builder().x(0)
                                            .y(0);
    }

    public Point subtract(final Point right) {
        return Point.create(getX() - right.getX(),
                            getY() - right.getY());
    }

    public abstract Builder toBuilder();

    @AutoValue.Builder
    public interface Builder {
        Builder x(int x);

        Builder y(int y);

        Point build();
    }
}
