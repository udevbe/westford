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
package org.westmalle.wayland.output.events;

import com.google.auto.value.AutoValue;
import org.westmalle.wayland.output.Point;

import javax.annotation.Nonnull;

@AutoValue
public abstract class Motion {

    public static Motion create(final int time,
                                @Nonnull final Point point) {
        return new AutoValue_Motion(time,
                                    point);
    }

    public static Motion create(final int time,
                                final int x,
                                final int y) {
        return new AutoValue_Motion(time,
                                    Point.create(x,
                                                 y));
    }

    public abstract int getTime();

    @Nonnull
    public abstract Point getPoint();
}
