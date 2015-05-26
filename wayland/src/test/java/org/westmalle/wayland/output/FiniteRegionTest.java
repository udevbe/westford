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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.westmalle.wayland.nativ.Libpixman1;
import org.westmalle.wayland.nativ.pixman_region32;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;


@RunWith(MockitoJUnitRunner.class)
public class FiniteRegionTest {

    @Mock
    private Libpixman1   libpixman1;
    @InjectMocks
    private FiniteRegion finiteRegion;

    @Test
    public void testAdd() throws Exception {
        //given
        final Rectangle rectangle = Rectangle.builder()
                                             .width(100)
                                             .height(100)
                                             .build();
        //when
        this.finiteRegion.add(rectangle);
        //then
        verify(this.libpixman1).pixman_region32_union_rect(this.finiteRegion.getPixmanRegion32(),
                                                           this.finiteRegion.getPixmanRegion32(),
                                                           rectangle.getX(),
                                                           rectangle.getY(),
                                                           rectangle.getWidth(),
                                                           rectangle.getHeight());
    }

    @Test
    public void testSubtract() throws Exception {
        //given
        final Rectangle rectangle = Rectangle.builder()
                                             .width(100)
                                             .height(100)
                                             .build();
        //when
        this.finiteRegion.subtract(rectangle);
        //then
        final ArgumentCaptor<pixman_region32> delta_pixman_region32Captor = ArgumentCaptor.forClass(pixman_region32.class);
        verify(this.libpixman1).pixman_region32_init_rect(delta_pixman_region32Captor.capture(),
                                                          eq(rectangle.getX()),
                                                          eq(rectangle.getY()),
                                                          eq(rectangle.getWidth()),
                                                          eq(rectangle.getHeight()));
        verify(this.libpixman1).pixman_region32_subtract(this.finiteRegion.getPixmanRegion32(),
                                                         delta_pixman_region32Captor.getValue(),
                                                         delta_pixman_region32Captor.getValue());
    }

    @Test
    public void testContains() throws Exception {
        //given
        final Point point = Point.create(50,
                                         50);
        //when
        this.finiteRegion.contains(point);
        //then
        verify(this.libpixman1).pixman_region32_contains_point(this.finiteRegion.getPixmanRegion32(),
                                                               point.getX(),
                                                               point.getY(),
                                                               null);
    }

    @Test
    public void testContainsWithClipping() throws Exception {
        //given
        final Rectangle clipping = Rectangle.create(60,
                                                    60,
                                                    10,
                                                    10);
        final Point point = Point.create(60,
                                         60);
        //when
        this.finiteRegion.contains(clipping,
                                   point);
        //then
        verify(this.libpixman1).pixman_region32_intersect_rect(this.finiteRegion.getPixmanRegion32(),
                                                               this.finiteRegion.getPixmanRegion32(),
                                                               clipping.getX(),
                                                               clipping.getY(),
                                                               clipping.getWidth(),
                                                               clipping.getHeight());
        verify(this.libpixman1).pixman_region32_contains_point(this.finiteRegion.getPixmanRegion32(),
                                                               point.getX(),
                                                               point.getY(),
                                                               null);
    }
}