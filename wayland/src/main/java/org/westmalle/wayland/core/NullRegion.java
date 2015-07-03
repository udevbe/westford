package org.westmalle.wayland.core;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class NullRegion implements Region{

    @Inject
    NullRegion() {
    }

    @Nonnull
    @Override
    public List<Rectangle> asList() {
        return Collections.emptyList();
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
        return false;
    }

    @Override
    public boolean contains(@Nonnull final Rectangle clipping, @Nonnull final Point point) {
        return false;
    }
}
