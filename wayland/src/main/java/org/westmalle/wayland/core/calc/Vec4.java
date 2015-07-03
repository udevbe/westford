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
package org.westmalle.wayland.core.calc;

import com.google.auto.value.AutoValue;

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
        //TODO unit test
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
        //TODO unit test
        return Vec4.create(getX() - right.getX(),
                           getY() - right.getY(),
                           getZ() - right.getZ(),
                           getW() - right.getW());
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
