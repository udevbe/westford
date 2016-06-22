//Copyright 2016 Erik De Rijcke
//
//Licensed under the Apache License,Version2.0(the"License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
//http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing,software
//distributed under the License is distributed on an"AS IS"BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
package org.westmalle.wayland.core;

import com.google.auto.value.AutoValue;
import org.westmalle.wayland.core.calc.Vec4;

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
