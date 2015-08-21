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

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.List;

/**
 * x: -32768
 * y: -32768
 * width: 0x7fffffff
 * height: 0x7fffffff
 */
@Singleton
public class InfiniteRegion implements Region {
    @Nonnull
    private static final List<Rectangle> INFINITE_RECT = Collections.singletonList(Rectangle.create(Short.MIN_VALUE,
                                                                                                    Short.MIN_VALUE,
                                                                                                    Integer.MAX_VALUE,
                                                                                                    Integer.MAX_VALUE));
    @Nonnull
    private final FiniteRegionFactory finiteRegionFactory;

    @Inject
    InfiniteRegion(@Nonnull final FiniteRegionFactory finiteRegionFactory) {
        this.finiteRegionFactory = finiteRegionFactory;
    }

    @Nonnull
    @Override
    public List<Rectangle> asList() {
        return INFINITE_RECT;
    }

    @Nonnull
    @Override
    public Region add(@Nonnull final Rectangle rectangle) {
        return this;
    }

    @Nonnull
    @Override
    public Region subtract(@Nonnull final Rectangle rectangle) {
        return this;
    }

    @Override
    public boolean contains(@Nonnull final Point point) {
        return true;
    }

    @Override
    public boolean contains(@Nonnull final Rectangle clipping,
                            @Nonnull final Point point) {
        return this.finiteRegionFactory.create()
                                       .add(clipping)
                                       .contains(point);
    }
}
