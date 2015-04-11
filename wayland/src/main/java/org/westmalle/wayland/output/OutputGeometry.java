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
package org.westmalle.wayland.output;

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
