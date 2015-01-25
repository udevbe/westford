package org.trinity.wayland.protocol;

import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.WlCompositorResource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.trinity.wayland.output.Compositor;
import org.trinity.wayland.output.RegionFactory;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
                        WlSurfaceFactory.class,
                        WlRegionFactory.class,
                        RegionFactory.class,
                })
public class WlCompositorTest {

    @Mock
    private Display          display;
    @Mock
    private WlSurfaceFactory wlSurfaceFactory;
    @Mock
    private WlRegionFactory  wlRegionFactory;
    @Mock
    private RegionFactory    pixmanRegionFactory;
    @Mock
    private Compositor       compositor;

    @InjectMocks
    private WlCompositor wlCompositor;

    @Test
    public void testOnBindClient() throws Exception {
        final WlCompositorResource wlCompositorResource = this.wlCompositor.onBindClient(mock(Client.class),
                                                                                         1,
                                                                                         1);
        assertThat(wlCompositorResource).isNotNull();
    }

    @Test
    public void testCreateSurface() throws Exception {

    }

    @Test
    public void testCreateRegion() throws Exception {

    }

    @Test
    public void testGetResources() throws Exception {

    }

    @Test
    public void testCreate() throws Exception {

    }
}