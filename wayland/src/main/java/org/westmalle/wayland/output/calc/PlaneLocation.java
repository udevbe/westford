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

package org.westmalle.wayland.output.calc;

import com.google.auto.value.AutoValue;

import javax.annotation.Nonnull;
import java.util.Optional;

@AutoValue
public abstract class PlaneLocation {

    public static PlaneLocation create(@Nonnull final Vec4 point,
                                       @Nonnull final Plane plane) {
        return new AutoValue_PlaneLocation(point,
                                           plane);
    }

    @Nonnull
    public abstract Vec4 getLocation();

    @Nonnull
    public abstract Plane getPlane();

    @Nonnull
    public Optional<PlaneLocation> translateTo(@Nonnull final Plane targetPlane) {
        //TODO unit test

        final Optional<Mat4>          mat4Optional = getPlane().getTranslation(targetPlane);
        final Optional<PlaneLocation> result;
        if (mat4Optional.isPresent()) {
            result = Optional.of(targetPlane.locate(mat4Optional.get()
                                                                .multiply(getLocation())));
        }
        else {
            result = Optional.empty();
        }
        return result;
    }
}
