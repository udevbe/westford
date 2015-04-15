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
package org.westmalle.wayland.output;

import com.google.auto.factory.AutoFactory;
import com.sun.jna.ptr.IntByReference;
import org.freedesktop.pixman1.Pixman1Library;
import org.freedesktop.pixman1.pixman_box32;
import org.freedesktop.pixman1.pixman_region32;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@AutoFactory(className = "RegionFactory")
public class Region {

    private static final class InfiniteRegion extends Region {
        @Nonnull
        private static final List<Rectangle> INFINITE_RECT = Collections.singletonList(Rectangle.create(Short.MIN_VALUE,
                                                                                                        Short.MIN_VALUE,
                                                                                                        Integer.MAX_VALUE,
                                                                                                        Integer.MAX_VALUE));

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
            return new Region().add(clipping)
                               .contains(point);
        }

        @Override
        public boolean contains(@Nonnull final Point point) {
            return true;
        }
    }

    /**
     * x: -32768
     * y: -32768
     * width: 0x7fffffff
     * height: 0x7fffffff
     */
    public static final Region INFINITY = new InfiniteRegion();

    private final pixman_region32 pixman_region32 = new pixman_region32();

    Region() {
    }

    @Nonnull
    public List<Rectangle> asList() {
        //int pointer
        final IntByReference n_rects = new IntByReference();
        final pixman_box32 pixman_box32_array = Pixman1Library.INSTANCE
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
    public Region add(@Nonnull final Rectangle rectangle) {
        Pixman1Library.INSTANCE
                .pixman_region32_union_rect(getPixmanRegion32(),
                                            getPixmanRegion32(),
                                            rectangle.getX(),
                                            rectangle.getY(),
                                            rectangle.getWidth(),
                                            rectangle.getHeight());

        return this;
    }

    @Nonnull
    public Region subtract(@Nonnull final Rectangle rectangle) {
        final pixman_region32 delta_pixman_region32 = new pixman_region32();
        Pixman1Library.INSTANCE
                .pixman_region32_init_rect(delta_pixman_region32,
                                           rectangle.getX(),
                                           rectangle.getY(),
                                           rectangle.getWidth(),
                                           rectangle.getHeight());
        Pixman1Library.INSTANCE
                .pixman_region32_subtract(getPixmanRegion32(),
                                          getPixmanRegion32(),
                                          delta_pixman_region32);
        return this;
    }

    public boolean contains(@Nonnull final Point point) {
        return Pixman1Library.INSTANCE
                       .pixman_region32_contains_point(getPixmanRegion32(),
                                                       point.getX(),
                                                       point.getY(),
                                                       null) != 0;
    }

    public boolean contains(@Nonnull final Rectangle clipping,
                            @Nonnull final Point point) {
        //fast path
        if (clipping.getWidth() == 0 && clipping.getHeight() == 0) {
            return false;
        }
        Pixman1Library.INSTANCE
                .pixman_region32_intersect_rect(getPixmanRegion32(),
                                                getPixmanRegion32(),
                                                clipping.getX(),
                                                clipping.getY(),
                                                clipping.getWidth(),
                                                clipping.getHeight());
        return Pixman1Library.INSTANCE
                       .pixman_region32_contains_point(getPixmanRegion32(),
                                                       point.getX(),
                                                       point.getY(),
                                                       null) != 0;
    }

    @Nonnull
    public pixman_region32 getPixmanRegion32() {
        return this.pixman_region32;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) { return true; }
        if (!(o instanceof Region)) { return false; }

        final Region region = (Region) o;

        return (region.asList()
                      .containsAll(asList())
                && asList().containsAll(region.asList()));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(asList());
    }
}
