package org.westmalle.wayland.output;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(FiniteRegionFactory.class)
public class InfiniteRegionTest {

    @Mock
    private FiniteRegionFactory finiteRegionFactory;
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
        final Point point = Point.create(123,
                456);
        final Rectangle rectangle = Rectangle.create(123,
                                                     456,
                                                     789,
                                                     12);
        final FiniteRegion finiteRegion = mock(FiniteRegion.class);
        when(this.finiteRegionFactory.create()).thenReturn(finiteRegion);
        when(finiteRegion.add(rectangle)).thenReturn(finiteRegion);
        //when
        region.contains(rectangle,
                point);
        //then
        verify(this.finiteRegionFactory).create();
        verify(finiteRegion).add(rectangle);
        verify(finiteRegion).contains(point);

    }
}