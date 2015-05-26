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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(FiniteRegionFactory.class)
public class InfiniteRegionTest {

    @Mock
    private FiniteRegionFactory finiteRegionFactory;
    @InjectMocks
    private InfiniteRegion      region;

    @Test
    public void testAsList() throws Exception {
        //given
        //when
        final List<Rectangle> rectangles = this.region.asList();
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
        final Region add = this.region.add(rectangle);
        //then
        assertThat(add).isEqualTo(this.region);
    }

    @Test
    public void testSubtract() throws Exception {
        //given
        final Rectangle rectangle = Rectangle.create(123,
                                                     456,
                                                     789,
                                                     12);
        //when
        final Region subtract = this.region.subtract(rectangle);
        //then
        assertThat(subtract).isEqualTo(this.region);
    }

    @Test
    public void testContains() throws Exception {
        //given
        //when
        final boolean contains = this.region.contains(Point.create(Short.MAX_VALUE,
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
        this.region.contains(rectangle,
                             point);
        //then
        verify(this.finiteRegionFactory).create();
        verify(finiteRegion).add(rectangle);
        verify(finiteRegion).contains(point);

    }
}