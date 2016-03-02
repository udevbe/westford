package org.westmalle.wayland.core;

import com.github.zubnix.jaccall.Pointer;
import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import org.westmalle.wayland.nativ.libpixman1.Libpixman1;
import org.westmalle.wayland.nativ.libpixman1.pixman_box32;
import org.westmalle.wayland.nativ.libpixman1.pixman_region32;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.github.zubnix.jaccall.Pointer.malloc;

@AutoFactory(className = "FiniteRegionFactory",
             allowSubclasses = true)
public class FiniteRegion implements Region {

    private final Libpixman1               libpixman1;
    private final Pointer<pixman_region32> pixman_region32Pointer;

    FiniteRegion(@Provided final Libpixman1 libpixman1) {
        this.libpixman1 = libpixman1;
        this.pixman_region32Pointer = malloc(pixman_region32.SIZE,
                                             pixman_region32.class);
        this.libpixman1.pixman_region32_init(this.pixman_region32Pointer.address);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(asList());
    }

    @Nonnull
    @Override
    public List<Rectangle> asList() {
        //int pointer
        final Pointer<Integer> n_rects = Pointer.nref(0);
        final Pointer<pixman_box32> pixman_box32_array = Pointer.wrap(pixman_box32.class,
                                                                      this.libpixman1.pixman_region32_rectangles(this.pixman_region32Pointer.address,
                                                                                                                 n_rects.address));
        final int             size  = n_rects.dref();
        final List<Rectangle> boxes = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            final pixman_box32 pixman_box32 = pixman_box32_array.dref(i);
            final int          x            = pixman_box32.x1();
            final int          y            = pixman_box32.y1();

            final int width  = pixman_box32.x2() - x;
            final int height = pixman_box32.y2() - y;
            boxes.add(Rectangle.create(x,
                                       y,
                                       width,
                                       height));
        }
        return boxes;
    }

    @Nonnull
    public Pointer<pixman_region32> getPixmanRegion32() {
        return this.pixman_region32Pointer;
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
        this.libpixman1.pixman_region32_union_rect(this.pixman_region32Pointer.address,
                                                   this.pixman_region32Pointer.address,
                                                   rectangle.getX(),
                                                   rectangle.getY(),
                                                   rectangle.getWidth(),
                                                   rectangle.getHeight());

        return this;
    }

    @Nonnull
    @Override
    public Region subtract(@Nonnull final Rectangle rectangle) {
        final Pointer<pixman_region32> delta_pixman_region32 = Pointer.ref(new pixman_region32());
        this.libpixman1.pixman_region32_init_rect(delta_pixman_region32.address,
                                                  rectangle.getX(),
                                                  rectangle.getY(),
                                                  rectangle.getWidth(),
                                                  rectangle.getHeight());
        this.libpixman1.pixman_region32_subtract(this.pixman_region32Pointer.address,
                                                 this.pixman_region32Pointer.address,
                                                 delta_pixman_region32.address);
        return this;
    }

    @Override
    public boolean contains(@Nonnull final Point point) {
        return this.libpixman1.pixman_region32_contains_point(this.pixman_region32Pointer.address,
                                                              point.getX(),
                                                              point.getY(),
                                                              0L) != 0;
    }

    @Override
    public boolean contains(@Nonnull final Rectangle clipping,
                            @Nonnull final Point point) {
        //fast path
        if (clipping.getWidth() == 0 && clipping.getHeight() == 0) {
            return false;
        }
        this.libpixman1.pixman_region32_intersect_rect(this.pixman_region32Pointer.address,
                                                       this.pixman_region32Pointer.address,
                                                       clipping.getX(),
                                                       clipping.getY(),
                                                       clipping.getWidth(),
                                                       clipping.getHeight());
        return this.libpixman1.pixman_region32_contains_point(this.pixman_region32Pointer.address,
                                                              point.getX(),
                                                              point.getY(),
                                                              0L) != 0;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        this.pixman_region32Pointer.close();
    }
}
