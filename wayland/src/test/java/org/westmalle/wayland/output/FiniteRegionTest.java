package org.westmalle.wayland.output;

import com.sun.jna.Native;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.westmalle.wayland.platform.pixman1.Libpixman1;

import java.util.List;

import static com.google.common.truth.Truth.assertThat;


@RunWith(MockitoJUnitRunner.class)
public class FiniteRegionTest {

    private FiniteRegion finiteRegion;

    @Before
    public void setUp() throws Exception {

        this.finiteRegion = new FiniteRegion(new Libpixman1());
    }

    @Test
    public void testAdd() throws Exception {
        //given
        final Rectangle rect0 = Rectangle.builder()
                                         .width(100)
                                         .height(100)
                                         .build();
        final Rectangle rect1 = Rectangle.create(50,
                                                 50,
                                                 100,
                                                 100);
        this.finiteRegion.add(rect0);
        //when
        this.finiteRegion.add(rect1);
        //then
        final List<Rectangle> Rectangles = this.finiteRegion.asList();
        assertThat(Rectangles).hasSize(3);
        assertThat(Rectangles.get(0)).isEqualTo(Rectangle.builder()
                                                         .width(100)
                                                         .height(50)
                                                         .build());
        assertThat(Rectangles.get(1)).isEqualTo(Rectangle.builder()
                                                         .y(50)
                                                         .width(150)
                                                         .height(50)
                                                         .build());
        assertThat(Rectangles.get(2)).isEqualTo(Rectangle.create(50,
                                                                 100,
                                                                 100,
                                                                 50));
    }

    @Test
    public void testSubtract() throws Exception {
        //given
        final Rectangle rect0 = Rectangle.builder()
                                         .width(100)
                                         .height(100)
                                         .build();
        final Rectangle rect1 = Rectangle.create(50,
                                                 50,
                                                 100,
                                                 100);
        this.finiteRegion.add(rect0);
        //when
        this.finiteRegion.subtract(rect1);
        //then
        final List<Rectangle> Rectangles = this.finiteRegion.asList();
        assertThat(Rectangles).hasSize(2);
        assertThat(Rectangles.get(0)).isEqualTo(Rectangle.builder()
                                                         .width(100)
                                                         .height(50)
                                                         .build());
        assertThat(Rectangles.get(1)).isEqualTo(Rectangle.builder()
                                                         .y(50)
                                                         .width(50)
                                                         .height(50)
                                                         .build());
    }

    @Test
    public void testContains() throws Exception {
        //given
        final Rectangle rect0 = Rectangle.create(50,
                                                 50,
                                                 100,
                                                 100);
        this.finiteRegion.add(rect0);
        //when
        final boolean contains = this.finiteRegion.contains(Point.create(50,
                                                                   50));
        final boolean notContains = this.finiteRegion.contains(Point.create(151,
                                                                      151));
        //then
        assertThat(contains).isTrue();
        assertThat(notContains).isFalse();
    }

    @Test
    public void testContainsWithClipping() throws Exception {
        //given
        final Rectangle clipping = Rectangle.create(60,
                                                    60,
                                                    10,
                                                    10);
        final Rectangle rect0 = Rectangle.create(50,
                                                 50,
                                                 100,
                                                 100);
        this.finiteRegion.add(rect0);
        //when
        final boolean contains = this.finiteRegion.contains(clipping,
                                                      Point.create(60,
                                                                   60));
        final boolean notContains = this.finiteRegion.contains(clipping,
                                                         Point.create(71,
                                                                      71));
        //then
        assertThat(contains).isTrue();
        assertThat(notContains).isFalse();
    }
}