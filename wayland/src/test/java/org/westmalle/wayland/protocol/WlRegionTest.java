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
package org.westmalle.wayland.protocol;

import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.WlRegionResource;
import org.freedesktop.wayland.server.jna.WaylandServerLibrary;
import org.freedesktop.wayland.server.jna.WaylandServerLibraryMapping;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.westmalle.wayland.output.Rectangle;
import org.westmalle.wayland.output.Region;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(WaylandServerLibrary.class)
public class WlRegionTest {

    @Mock
    private WaylandServerLibraryMapping waylandServerLibraryMapping;

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(WaylandServerLibrary.class);
        when(WaylandServerLibrary.INSTANCE()).thenReturn(this.waylandServerLibraryMapping);
    }

    @Test
    public void testCreate() throws Exception {
        //given
        final Client   client   = mock(Client.class);
        final int      version  = 1;
        final int      id       = 1;
        final Region   region   = mock(Region.class);
        final WlRegion wlRegion = new WlRegion(region);
        //when
        final WlRegionResource wlRegionResource = wlRegion.create(client,
                                                                  version,
                                                                  id);
        //then
        assertThat(wlRegionResource).isNotNull();
        assertThat(wlRegionResource.getImplementation()).isSameAs(wlRegion);
    }

    @Test
    public void testDestroy() throws Exception {
        //given
        final WlRegionResource wlRegionResource = mock(WlRegionResource.class);
        final Region           region           = mock(Region.class);
        final WlRegion         wlRegion         = new WlRegion(region);
        //when
        wlRegion.destroy(wlRegionResource);
        //then
        verify(wlRegionResource).destroy();
    }

    @Test
    public void testAdd() throws Exception {
        //given
        final WlRegionResource wlRegionResource = mock(WlRegionResource.class);
        final Region           region           = mock(Region.class);
        final WlRegion         wlRegion         = new WlRegion(region);
        final int              x                = 10;
        final int              y                = -12;
        final int              width            = 123;
        final int              height           = 111;
        //when
        wlRegion.add(wlRegionResource,
                     x,
                     y,
                     width,
                     height);
        //then
        verify(region).add(eq(Rectangle.create(x,
                                               y,
                                               width,
                                               height)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNegativeWidthHeight() throws Exception {
        //given
        final WlRegionResource wlRegionResource = mock(WlRegionResource.class);
        final Region           region           = mock(Region.class);
        final WlRegion         wlRegion         = new WlRegion(region);
        final int              x                = 10;
        final int              y                = 12;
        final int              width            = -100;
        final int              height           = -200;
        //when
        wlRegion.add(wlRegionResource,
                     x,
                     y,
                     width,
                     height);
        //then
    }

    @Test
    public void testSubtract() throws Exception {
        //given
        final WlRegionResource wlRegionResource = mock(WlRegionResource.class);
        final Region           region           = mock(Region.class);
        final WlRegion         wlRegion         = new WlRegion(region);
        final int              x                = -12;
        final int              y                = -10;
        final int              width            = 321;
        final int              height           = 111;
        //when
        wlRegion.subtract(wlRegionResource,
                          x,
                          y,
                          width,
                          height);
        //then
        verify(region).subtract(eq(Rectangle.create(x,
                                                    y,
                                                    width,
                                                    height)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubtractNativeWidthHeight() throws Exception {
        //given
        final WlRegionResource wlRegionResource = mock(WlRegionResource.class);
        final Region           region           = mock(Region.class);
        final WlRegion         wlRegion         = new WlRegion(region);
        final int              x                = 22;
        final int              y                = 11;
        final int              width            = -222;
        final int              height           = -333;
        //when
        wlRegion.subtract(wlRegionResource,
                          x,
                          y,
                          width,
                          height);
        //then
    }
}