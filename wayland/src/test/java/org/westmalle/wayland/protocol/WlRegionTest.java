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
import org.westmalle.wayland.output.Region;

import javax.media.nativewindow.util.Rectangle;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
        //following classes have static methods, so we have to powermock them:
        WaylandServerLibrary.class,
})
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
        final Client client = mock(Client.class);
        final int version = 1;
        final int id = 1;
        final Region region = mock(Region.class);
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
        final Region region = mock(Region.class);
        final WlRegion wlRegion = new WlRegion(region);
        //when
        wlRegion.destroy(wlRegionResource);
        //then
        verify(wlRegionResource,times(1)).destroy();
    }

    @Test
    public void testAdd() throws Exception {
        //given
        final WlRegionResource wlRegionResource = mock(WlRegionResource.class);
        final Region region = mock(Region.class);
        final WlRegion wlRegion = new WlRegion(region);
        final int x = 10;
        final int y = -12;
        final int width = 123;
        final int height = 111;
        //when
        wlRegion.add(wlRegionResource,
                     x,
                     y,
                     width,
                     height);
        //then
        verify(region,
               times(1)).add(eq(new Rectangle(x,
                                              y,
                                              width,
                                              height)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNegativeWidthHeight() throws Exception {
        //given
        final WlRegionResource wlRegionResource = mock(WlRegionResource.class);
        final Region region = mock(Region.class);
        final WlRegion wlRegion = new WlRegion(region);
        final int x = 10;
        final int y = 12;
        final int width = -100;
        final int height = -200;
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
        final Region region = mock(Region.class);
        final WlRegion wlRegion = new WlRegion(region);
        final int x = -12;
        final int y = -10;
        final int width = 321;
        final int height = 111;
        //when
        wlRegion.subtract(wlRegionResource,
                          x,
                          y,
                          width,
                          height);
        //then
        verify(region,
               times(1)).subtract(eq(new Rectangle(x,
                                                   y,
                                                   width,
                                                   height)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubtractNativeWidthHeight() throws Exception {
        //given
        final WlRegionResource wlRegionResource = mock(WlRegionResource.class);
        final Region region = mock(Region.class);
        final WlRegion wlRegion = new WlRegion(region);
        final int x = 22;
        final int y = 11;
        final int width = -222;
        final int height = -333;
        //when
        wlRegion.subtract(wlRegionResource,
                          x,
                          y,
                          width,
                          height);
        //then
    }
}