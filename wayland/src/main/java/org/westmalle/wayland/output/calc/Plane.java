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

import org.westmalle.wayland.output.Point;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

public class Plane {

    private final Map<Plane, Mat4> translations = new WeakHashMap<>();

    @Nonnull
    public PlaneLocation locate(@Nonnull final Point point) {
        return locate(Vec4.create(point.getX(),
                                  point.getY(),
                                  0,
                                  1));
    }

    @Nonnull
    public PlaneLocation locate(@Nonnull final Vec4 vec4) {
        return PlaneLocation.create(vec4,
                                    this);
    }

    @Nonnull
    public Optional<Mat4> getTranslation(@Nonnull final Plane target) {
        return Optional.ofNullable(this.translations.get(target));
    }

    public void setTranslation(@Nonnull final Plane target,
                               @Nonnull final Mat4 translation) {
        this.translations.put(target,
                              translation);
        target.translations.put(this,
                                translation.invert());
    }
}