//Copyright 2015 Erik De Rijcke
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
