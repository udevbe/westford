package org.westmalle.wayland.protocol;

import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.WlCompositorResource;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.freedesktop.wayland.server.jna.WaylandServerLibrary;
import org.freedesktop.wayland.server.jna.WaylandServerLibraryMapping;
import org.freedesktop.wayland.shared.WlOutputTransform;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.westmalle.wayland.output.Surface;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
                        //following classes have static methods, so we have to powermock them:
                        WaylandServerLibrary.class,
                        //following classes are final, so we have to powermock them:
                        WlCallbackFactory.class
                })
public class WlSurfaceTest {

    @Mock
    private WlCallbackFactory    wlCallbackFactory;
    @Mock
    private WlCompositorResource compositorResource;
    @Mock
    private Surface              surface;

    @Mock
    private WaylandServerLibraryMapping waylandServerLibraryMapping;

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(WaylandServerLibrary.class);
        when(WaylandServerLibrary.INSTANCE()).thenReturn(this.waylandServerLibraryMapping);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetBufferTransform() throws Exception {
        //given
        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
        final int transform = WlOutputTransform.NORMAL.getValue();

        final WlSurface wlSurface = new WlSurface(this.wlCallbackFactory,
                                                  this.compositorResource,
                                                  this.surface);

        //when
        wlSurface.setBufferTransform(wlSurfaceResource,
                                     transform);
    }

    @Test
    public void testCreate() throws Exception {
        //given
        final Client client = mock(Client.class);
        final int version = 5;
        final int id = 100;

        final WlSurface wlSurface = new WlSurface(this.wlCallbackFactory,
                                                  this.compositorResource,
                                                  this.surface);
        //when
        final WlSurfaceResource wlSurfaceResource = wlSurface.add(client,
                                                                  version,
                                                                  id);
        //then
        assertThat(wlSurfaceResource).isNotNull();
        assertThat(wlSurfaceResource.getImplementation()).isSameAs(wlSurface);
    }

    @Test
    public void testDestroy() throws Exception {
        //given
        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);

        final WlSurface wlSurface = new WlSurface(this.wlCallbackFactory,
                                                  this.compositorResource,
                                                  this.surface);
        //when
        wlSurface.destroy(wlSurfaceResource);
        //then
        verify(wlSurfaceResource,
               times(1)).destroy();
    }

    @Test
    public void testAttach() throws Exception {

    }

    @Test
    public void testDamage() throws Exception {

    }

    @Test
    public void testFrame() throws Exception {

    }

    @Test
    public void testSetOpaqueRegion() throws Exception {

    }

    @Test
    public void testSetInputRegion() throws Exception {

    }

    @Test
    public void testCommit() throws Exception {

    }
}