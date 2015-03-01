package org.westmalle.wayland.output;

import org.junit.Test;

import java.util.List;

import static com.google.common.truth.Truth.assertThat;

public class InfiniteRegionTest {

    @Test
    public void testAsList() throws Exception {
        //given
        final Region region = Region.INFINITY;
        //when
        final List<Rectangle> rectangles = region.asList();
        //then
        assertThat(rectangles).hasSize(1);
    }

    @Test
    public void testAdd() throws Exception {
        //given
        final Region region = Region.INFINITY;
        final Rectangle rectangle = Rectangle.create(123,
                                                     456,
                                                     789,
                                                     12);
        //when
        final Region add = region.add(rectangle);
        //then
        assertThat(add).isEqualTo(region);
    }

    @Test
    public void testSubtract() throws Exception {
        //given
        final Region region = Region.INFINITY;
        final Rectangle rectangle = Rectangle.create(123,
                                                     456,
                                                     789,
                                                     12);
        //when
        final Region subtract = region.subtract(rectangle);
        //then
        assertThat(subtract).isEqualTo(region);
    }

    @Test
    public void testContains() throws Exception {
        //given
        final Region region = Region.INFINITY;
        //when
        final boolean contains = region.contains(Point.create(Short.MAX_VALUE,
                                                              Short.MAX_VALUE));
        //then
        assertThat(contains).isTrue();
    }

    @Test
    public void testContainsClipping() throws Exception {
        //given
        final Region region = Region.INFINITY;
        final Rectangle rectangle = Rectangle.create(123,
                                                     456,
                                                     789,
                                                     12);
        //when
        final boolean contains = region.contains(rectangle,
                                                 Point.create(123,
                                                              456));
        final boolean notContains = region.contains(rectangle,
                                                    Point.create(123,
                                                                 469));
        //then
        assertThat(contains).isTrue();
        assertThat(notContains).isFalse();
    }
}