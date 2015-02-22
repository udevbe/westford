package org.westmalle.wayland.protocol;

import com.sun.jna.Pointer;
import org.freedesktop.wayland.server.*;
import org.freedesktop.wayland.server.jna.WaylandServerLibrary;
import org.freedesktop.wayland.server.jna.WaylandServerLibraryMapping;
import org.freedesktop.wayland.util.InterfaceMeta;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.westmalle.wayland.output.*;

import java.util.LinkedList;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
                        //following classes have static methods, so we have to powermock them:
                        WaylandServerLibrary.class,
                        InterfaceMeta.class,
                        //following classes are final, so we have to powermock them:
                        WlSurfaceFactory.class,
                        WlRegionFactory.class,
                        RegionFactory.class,
                        SurfaceFactory.class
                })
public class WlCompositorTest {

    @Mock
    private Display          display;
    @Mock
    private WlSurfaceFactory wlSurfaceFactory;
    @Mock
    private WlRegionFactory  wlRegionFactory;
    @Mock
    private RegionFactory    regionFactory;
    @Mock
    private SurfaceFactory   surfaceFactory;
    @Mock
    private Compositor       compositor;

    @Mock
    private WaylandServerLibraryMapping waylandServerLibraryMapping;
    @Mock
    private InterfaceMeta               interfaceMeta;
    @Mock
    private Pointer                     globalPointer;

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(WaylandServerLibrary.class,
                                InterfaceMeta.class);
        when(InterfaceMeta.get((Class<?>) any())).thenReturn(this.interfaceMeta);
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
                                                           this.regionFactory,
                                                           this.surfaceFactory,
                                                           this.compositor);

        //when
        final WlCompositorResource wlCompositorResource = wlCompositor.onBindClient(mock(Client.class),
                                                                                    1,
                                                                                    1);
        //then
        assertThat(wlCompositorResource).isNotNull();
        assertThat(wlCompositorResource.getImplementation()).isSameAs(wlCompositor);
    }

    @Test
    public void testCreateSurface() throws Exception {
        //given
        LinkedList<WlSurfaceResource> surfacesStack = new LinkedList<>();
        when(this.compositor.getSurfacesStack()).thenReturn(surfacesStack);

        final WlSurface wlSurface = mock(WlSurface.class);
        when(this.wlSurfaceFactory.create(any())).thenReturn(wlSurface);

        WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
        when(wlSurface.add(any(),
                           anyInt(),
                           anyInt())).thenReturn(wlSurfaceResource);

        WlCompositorResource wlCompositorResource = mock(WlCompositorResource.class);
        final Client client = mock(Client.class);
        when(wlCompositorResource.getClient()).thenReturn(client);
        final int version = 3;
        when(wlCompositorResource.getVersion()).thenReturn(version);

        final Surface surface = mock(Surface.class);
        when(this.surfaceFactory.create(wlCompositorResource)).thenReturn(surface);

        final WlCompositor wlCompositor = new WlCompositor(this.display,
                                                           this.wlSurfaceFactory,
                                                           this.wlRegionFactory,
                                                           this.regionFactory,
                                                           this.surfaceFactory,
                                                           this.compositor);
        //when
        final int id = 1;
        wlCompositor.createSurface(wlCompositorResource,
                                   1);
        //then
        verify(wlSurface,
               times(1)).add(client,
                             version,
                             id);

        assertThat((Iterable) surfacesStack).contains(wlSurfaceResource);

        ArgumentCaptor<Listener> destroyListenerCaptor = ArgumentCaptor.forClass(Listener.class);
        verify(wlSurfaceResource,
               times(1)).addDestroyListener(destroyListenerCaptor.capture());
        final Listener destroyListener = destroyListenerCaptor.getValue();

        //and later when
        destroyListener.handle();

        //then
        assertThat((Iterable) surfacesStack).doesNotContain(wlSurfaceResource);
        verify(this.compositor,
               times(1)).requestRender();
    }

    @Test
    public void testCreateRegion() throws Exception {
        //given
        final Region region = mock(Region.class);
        when(this.regionFactory.create()).thenReturn(region);

        final WlRegion wlRegion = mock(WlRegion.class);
        when(this.wlRegionFactory.create(any())).thenReturn(wlRegion);

        WlCompositorResource wlCompositorResource = mock(WlCompositorResource.class);
        final Client client = mock(Client.class);
        when(wlCompositorResource.getClient()).thenReturn(client);
        final int version = 2;
        when(wlCompositorResource.getVersion()).thenReturn(version);

        final WlCompositor wlCompositor = new WlCompositor(this.display,
                                                           this.wlSurfaceFactory,
                                                           this.wlRegionFactory,
                                                           this.regionFactory,
                                                           this.surfaceFactory,
                                                           this.compositor);
        //when
        final int id = 5;
        wlCompositor.createRegion(wlCompositorResource,
                                  id);
        //then
        verify(wlRegion,
               times(1)).add(client,
                             version,
                             id);
    }

    @Test
    public void testCreate() throws Exception {
        //given
        final WlCompositor wlCompositor = new WlCompositor(this.display,
                                                           this.wlSurfaceFactory,
                                                           this.wlRegionFactory,
                                                           this.regionFactory,
                                                           this.surfaceFactory,
                                                           this.compositor);
        final Client client = mock(Client.class);
        final int version = 1;
        final int id = 6;
        //when
        final WlCompositorResource wlCompositorResource = wlCompositor.create(client,
                                                                              version,
                                                                              id);
        //then
        assertThat(wlCompositorResource).isNotNull();
    }
}