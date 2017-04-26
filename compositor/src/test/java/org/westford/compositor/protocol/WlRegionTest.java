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
package org.westford.compositor.protocol;

import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.WlRegionResource;
import org.freedesktop.wayland.server.jaccall.WaylandServerCore;
import org.freedesktop.wayland.util.ObjectCache;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.westford.compositor.core.Rectangle;
import org.westford.compositor.core.Region;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(WaylandServerCore.class)
public class WlRegionTest {

    @Mock
    private WaylandServerCore waylandServerCore;
    @Mock
    private Region            region;

    private WlRegion wlRegion;

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(WaylandServerCore.class);
        when(WaylandServerCore.INSTANCE()).thenReturn(this.waylandServerCore);
        ObjectCache.remove(112358L);
        when(this.waylandServerCore.wl_resource_create(anyLong(),
                                                       anyLong(),
                                                       anyInt(),
                                                       anyInt())).thenReturn(112358L);
        this.wlRegion = new WlRegion(this.region);
    }

    @Test
    public void testCreate() throws Exception {
        //given
        final Client client = mock(Client.class);
        Whitebox.setInternalState(client,
                                  "pointer",
                                  2468L);
        final int version = 1;
        final int id      = 1;
        //when
        final WlRegionResource wlRegionResource = this.wlRegion.create(client,
                                                                       version,
                                                                       id);
        //then
        assertThat(wlRegionResource).isNotNull();
        assertThat(wlRegionResource.getImplementation()).isSameAs(this.wlRegion);
    }

    @Test
    public void testDestroy() throws Exception {
        //given
        final WlRegionResource wlRegionResource = mock(WlRegionResource.class);
        //when
        this.wlRegion.destroy(wlRegionResource);
        //then
        verify(wlRegionResource).destroy();
    }

    @Test
    public void testAdd() throws Exception {
        //given
        final WlRegionResource wlRegionResource = mock(WlRegionResource.class);
        final int              x                = 10;
        final int              y                = -12;
        final int              width            = 123;
        final int              height           = 111;
        //when
        this.wlRegion.add(wlRegionResource,
                          x,
                          y,
                          width,
                          height);
        //then
        verify(this.region).add(eq(Rectangle.Companion.create(x,
                                                              y,
                                                              width,
                                                              height)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNegativeWidthHeight() throws Exception {
        //given
        final WlRegionResource wlRegionResource = mock(WlRegionResource.class);
        final int              x                = 10;
        final int              y                = 12;
        final int              width            = -100;
        final int              height           = -200;
        //when
        this.wlRegion.add(wlRegionResource,
                          x,
                          y,
                          width,
                          height);
        //then
        //exception is thrown
    }

    @Test
    public void testSubtract() throws Exception {
        //given
        final WlRegionResource wlRegionResource = mock(WlRegionResource.class);
        final int              x                = -12;
        final int              y                = -10;
        final int              width            = 321;
        final int              height           = 111;
        //when
        this.wlRegion.subtract(wlRegionResource,
                               x,
                               y,
                               width,
                               height);
        //then
        verify(this.region).subtract(eq(Rectangle.Companion.create(x,
                                                                   y,
                                                                   width,
                                                                   height)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubtractNativeWidthHeight() throws Exception {
        //given
        final WlRegionResource wlRegionResource = mock(WlRegionResource.class);
        final int              x                = 22;
        final int              y                = 11;
        final int              width            = -222;
        final int              height           = -333;
        //when
        this.wlRegion.subtract(wlRegionResource,
                               x,
                               y,
                               width,
                               height);
        //then
        //exception is thrown
    }
}