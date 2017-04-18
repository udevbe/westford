/*
 * Westford Wayland Compositor.
 * Copyright (C) 2016  Erik De Rijcke
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.westford.compositor.core;

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

    @Override
    public void add(@Nonnull final Rectangle rectangle) {}

    @Override
    public void subtract(@Nonnull final Rectangle rectangle) {}

    @Override
    public boolean contains(@Nonnull final Point point) {
        return true;
    }

    @Override
    public boolean contains(@Nonnull final Rectangle clipping,
                            @Nonnull final Point point) {
        final FiniteRegion finiteRegion = this.finiteRegionFactory.create();
        finiteRegion.add(clipping);
        return finiteRegion.contains(point);
    }

    @Override
    public boolean contains(@Nonnull final Rectangle rectangle) {
        return true;
    }

    @Override
    public Region intersect(@Nonnull final Rectangle rectangle) {
        return this;
    }

    @Override
    public Region copy() {
        return this;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    //TODO equals & hash?
}
