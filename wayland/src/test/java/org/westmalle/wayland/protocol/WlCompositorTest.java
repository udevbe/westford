package org.westmalle.wayland.protocol;

import com.sun.jna.Pointer;
import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.WlCompositorResource;
import org.freedesktop.wayland.server.jna.WaylandServerLibrary;
import org.freedesktop.wayland.server.jna.WaylandServerLibraryMapping;
import org.freedesktop.wayland.util.InterfaceMeta;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.westmalle.wayland.output.Compositor;
import org.westmalle.wayland.output.RegionFactory;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
                        //following classes have static methods, so we have to powermock them:
                        WaylandServerLibrary.class,
                        InterfaceMeta.class,
                        //following classes are final, so we have to powermock them:
                        WlSurfaceFactory.class,
                        WlRegionFactory.class,
                        RegionFactory.class
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

    @Mock
    private WaylandServerLibraryMapping waylandServerLibraryMapping;
    @Mock
    private InterfaceMeta               interfaceMeta;
    @Mock
    private Pointer                     globalPointer;

    @BeforeClass
    public static void setUpClass() {
        PowerMockito.mockStatic(WaylandServerLibrary.class,
                                InterfaceMeta.class);
    }

    @Before
    public void setUp() throws Exception {
        when(InterfaceMeta.get(WlCompositorResource.class)).thenReturn(this.interfaceMeta);
        when(WaylandServerLibrary.INSTANCE()).thenReturn(this.waylandServerLibraryMapping);
        when(this.waylandServerLibraryMapping.wl_global_create(any(),
                                                               any(),
                                                               anyInt(),
                                                               any(),
                                                               any())).thenReturn(this.globalPointer);

    }

    @Test
    public void testOnBindClient() throws Exception {
        //given
        final Pointer resourcePointer = mock(Pointer.class);
        when(this.waylandServerLibraryMapping.wl_resource_create(any(),
                                                                 any(),
                                                                 anyInt(),
                                                                 anyInt())).thenReturn(resourcePointer);
        final WlCompositor wlCompositor = new WlCompositor(this.display,
                                                           this.wlSurfaceFactory,
                                                           this.wlRegionFactory,
                                                           this.pixmanRegionFactory,
                                                           this.compositor);

        //when
        final WlCompositorResource wlCompositorResource = wlCompositor.onBindClient(mock(Client.class),
                                                                                    1,
                                                                                    1);
        //then
        assertThat(wlCompositorResource).isNotNull();
        assertThat((Iterable) wlCompositor.getResources()).contains(wlCompositorResource);
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