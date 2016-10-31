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
package org.westford.compositor.core;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.List;

@Singleton
public class NullRegion implements Region {

    @Inject
    NullRegion() {
    }

    @Nonnull
    @Override
    public List<Rectangle> asList() {
        return Collections.emptyList();
    }

    @Override
    public void add(@Nonnull final Rectangle rectangle) {}

    @Override
    public void subtract(@Nonnull final Rectangle rectangle) {}

    @Override
    public boolean contains(@Nonnull final Point point) {
        return false;
    }

    @Override
    public boolean contains(@Nonnull final Rectangle clipping,
                            @Nonnull final Point point) {
        return false;
    }

    @Override
    public Region intersect(@Nonnull final Rectangle rectangle) {
        return this;
    }
}
