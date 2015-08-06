package org.westmalle.wayland.core;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import com.sun.jna.ptr.IntByReference;
import org.westmalle.wayland.nativ.libpixman1.Libpixman1;
import org.westmalle.wayland.nativ.libpixman1.pixman_box32;
import org.westmalle.wayland.nativ.libpixman1.pixman_region32;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@AutoFactory(className = "FiniteRegionFactory")
public class FiniteRegion implements Region {

    private final Libpixman1 libpixman1;
    private final pixman_region32 pixman_region32 = new pixman_region32();

    FiniteRegion(@Provided final Libpixman1 libpixman1) {
        this.libpixman1 = libpixman1;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(asList());
    }

    @Nonnull
    @Override
    public List<Rectangle> asList() {
        //int pointer
        final IntByReference n_rects = new IntByReference();
        final pixman_box32 pixman_box32_array = this.libpixman1
                .pixman_region32_rectangles(getPixmanRegion32(),
                                            n_rects);
        final int            size          = n_rects.getValue();
        final pixman_box32[] pixman_box32s = (pixman_box32[]) pixman_box32_array.toArray(size);

        final List<Rectangle> boxes = new ArrayList<>(size);
        for (final pixman_box32 pixman_box32 : pixman_box32s) {
            final int x = pixman_box32.x1;
            final int y = pixman_box32.y1;

            final int width = pixman_box32.x2 - x;
            final int height = pixman_box32.y2 - y;
            boxes.add(Rectangle.create(x,
                                       y,
                                       width,
                                       height));
        }
        return boxes;
    }

    @Nonnull
    public pixman_region32 getPixmanRegion32() {
        return this.pixman_region32;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Region)) {
            return false;
        }

        final Region region = (Region) o;

        return (region.asList()
                      .containsAll(asList())
                && asList().containsAll(region.asList()));
    }

    @Nonnull
    @Override
    public Region add(@Nonnull final Rectangle rectangle) {
        this.libpixman1.pixman_region32_union_rect(getPixmanRegion32(),
                                                   getPixmanRegion32(),
                                                   rectangle.getX(),
                                                   rectangle.getY(),
                                                   rectangle.getWidth(),
                                                   rectangle.getHeight());

        return this;
    }

    @Nonnull
    @Override
    public Region subtract(@Nonnull final Rectangle rectangle) {
        final pixman_region32 delta_pixman_region32 = new pixman_region32();
        this.libpixman1.pixman_region32_init_rect(delta_pixman_region32,
                                                  rectangle.getX(),
                                                  rectangle.getY(),
                                                  rectangle.getWidth(),
                                                  rectangle.getHeight());
        this.libpixman1.pixman_region32_subtract(getPixmanRegion32(),
                                                 getPixmanRegion32(),
                                                 delta_pixman_region32);
        return this;
    }

    @Override
    public boolean contains(@Nonnull final Point point) {
        return this.libpixman1.pixman_region32_contains_point(getPixmanRegion32(),
                                                              point.getX(),
                                                              point.getY(),
                                                              null) != 0;
    }

    @Override
    public boolean contains(@Nonnull final Rectangle clipping,
                            @Nonnull final Point point) {
        //fast path
        if (clipping.getWidth() == 0 && clipping.getHeight() == 0) {
            return false;
        }
        this.libpixman1.pixman_region32_intersect_rect(getPixmanRegion32(),
                                                       getPixmanRegion32(),
                                                       clipping.getX(),
                                                       clipping.getY(),
                                                       clipping.getWidth(),
                                                       clipping.getHeight());
        return this.libpixman1.pixman_region32_contains_point(getPixmanRegion32(),
                                                              point.getX(),
                                                              point.getY(),
                                                              null) != 0;
    }
}
