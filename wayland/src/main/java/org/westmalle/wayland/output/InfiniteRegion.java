package org.westmalle.wayland.output;

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
    public boolean contains(@Nonnull final Rectangle clipping,
                            @Nonnull final Point point) {
        return this.finiteRegionFactory.create()
                                       .add(clipping)
                                       .contains(point);
    }

    @Override
    public boolean contains(@Nonnull final Point point) {
        return true;
    }
}
