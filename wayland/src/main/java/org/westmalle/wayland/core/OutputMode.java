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
