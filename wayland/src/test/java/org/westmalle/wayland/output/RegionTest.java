package org.westmalle.wayland.output;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static com.google.common.truth.Truth.assertThat;


@RunWith(MockitoJUnitRunner.class)
public class RegionTest {

    @InjectMocks
    private Region region;

    @Test
    public void testAdd() throws Exception {
        //given
        final Rectangle rect0 = Rectangle.builder().x(0).
                                                       y(0).
                                                       width(100).
                                                       height(100).build();
        final Rectangle rect1 = Rectangle.builder().x(50).
                                                       y(50).
        width(100).
                height(100).build();
        this.region.add(rect0);
        //when
        this.region.add(rect1);
        //then
        final List<Rectangle> Rectangles = this.region.asList();
        assertThat(Rectangles).hasSize(3);
        assertThat(Rectangles.get(0)).isEqualTo(Rectangle.builder().x(0).
                                                        y(0).
                                                        width(100).
                height(50).build());
        assertThat(Rectangles.get(1)).isEqualTo(Rectangle.builder().x(0).
                                                        y(50).
                                                        width(150).
                height(50).build());
        assertThat(Rectangles.get(2)).isEqualTo(Rectangle.builder().x(50).
                                                        y(100).
                                                        width(100).
                height(50).build());
    }

    @Test
    public void testSubtract() throws Exception {
        //given
        final Rectangle rect0 = Rectangle.builder().x(0).
                y(0).
                width(100).
                height(100).build();
        final Rectangle rect1 = Rectangle.builder().x(50).
                y(50).
                width(100).
                height(100).build();
        this.region.add(rect0);
        //when
        this.region.subtract(rect1);
        //then
        final List<Rectangle> Rectangles = this.region.asList();
        assertThat(Rectangles).hasSize(2);
        assertThat(Rectangles.get(0)).isEqualTo(Rectangle.builder().x(0).
                                                        y(0).
                                                        width(100).
                height(50).build());
        assertThat(Rectangles.get(1)).isEqualTo(Rectangle.builder().x(0).
                                                        y(50).
                                                        width(50).
                height(50).build());
    }

    @Test
    public void testContains() throws Exception {
        //given
        final Rectangle rect0 = Rectangle.builder().x(50).
                y(50).
                width(100).
                height(100).build();
        this.region.add(rect0);
        //when
        final boolean contains = this.region.contains(Point.builder().x(50).
                                                                y(50).build());
        final boolean notContains = this.region.contains(Point.builder().x(151).
                                                                   y(151).build());
        //then
        assertThat(contains).isTrue();
        assertThat(notContains).isFalse();
    }
}