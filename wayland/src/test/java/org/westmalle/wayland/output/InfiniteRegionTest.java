package org.westmalle.wayland.output;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static com.google.common.truth.Truth.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class InfiniteRegionTest {

    @InjectMocks
    private InfiniteRegion region;

    @Test
    public void testAsList() throws Exception {
        //given
        //when
        final List<Rectangle> rectangles = region.asList();
        //then
        assertThat(rectangles).hasSize(1);
    }

    @Test
    public void testAdd() throws Exception {
        //given
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
        //when
        final boolean contains = region.contains(Point.create(Short.MAX_VALUE,
                                                              Short.MAX_VALUE));
        //then
        assertThat(contains).isTrue();
    }

    @Test
    public void testContainsClipping() throws Exception {
        //given
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