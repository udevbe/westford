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
                        WlSubSurfaceFactory.class
                })
public class WlSubCompositorTest {

    @Mock
    private Display             display;
    @Mock
    private WlSubSurfaceFactory wlSubSurfaceFactory;

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
    public void testDestroy() throws Exception {
        //given
        final WlSubcompositorResource subcompositorResource = mock(WlSubcompositorResource.class);

        final WlSubCompositor wlSubCompositor = new WlSubCompositor(this.display,
                                                                    this.wlSubSurfaceFactory);
        //when
        wlSubCompositor.destroy(subcompositorResource);
        //then
        verify(subcompositorResource,
               times(1)).destroy();
    }

    @Test
    public void testGetSubsurface() throws Exception {
        //given
        final Client client = mock(Client.class);
        final int version = 4;
        final WlSubcompositorResource subcompositorResource = mock(WlSubcompositorResource.class);
        when(subcompositorResource.getClient()).thenReturn(client);
        when(subcompositorResource.getVersion()).thenReturn(4);

        final int id = 123;
        final WlSurfaceResource surface = mock(WlSurfaceResource.class);
        final WlSurfaceResource parent = mock(WlSurfaceResource.class);

        final WlSubSurface wlSubSurface = mock(WlSubSurface.class);
        when(this.wlSubSurfaceFactory.create(surface,
                                             parent)).thenReturn(wlSubSurface);

        final WlSubsurfaceResource wlSubsurfaceResource = mock(WlSubsurfaceResource.class);
        when(wlSubSurface.add(any(),
                              anyInt(),
                              anyInt())).thenReturn(wlSubsurfaceResource);

        final WlSubCompositor wlSubCompositor = new WlSubCompositor(this.display,
                                                                    this.wlSubSurfaceFactory);
        //when
        wlSubCompositor.getSubsurface(subcompositorResource,
                                      id,
                                      surface,
                                      parent);
        //then
        verify(wlSubSurface,
               times(1)).add(client,
                             version,
                             id);
        final ArgumentCaptor<Listener> listenerArgumentCaptor = ArgumentCaptor.forClass(Listener.class);
        verify(surface,
               times(1)).addDestroyListener(listenerArgumentCaptor.capture());
        final Listener destroyListener = listenerArgumentCaptor.getValue();
        //and when
        destroyListener.handle();
        //then
        verify(wlSubsurfaceResource,
               times(1)).destroy();
    }

    @Test
    public void testCreate() throws Exception {
        //given
        final Client client = mock(Client.class);
        final int version = 2;
        final int id = 12;

        final WlSubCompositor wlSubCompositor = new WlSubCompositor(this.display,
                                                                    this.wlSubSurfaceFactory);
        //when
        final WlSubcompositorResource wlSubcompositorResource = wlSubCompositor.create(client,
                                                                                       version,
                                                                                       id);
        //then
        assertThat(wlSubcompositorResource).isNotNull();
        assertThat(wlSubcompositorResource.getImplementation()).isSameAs(wlSubCompositor);
    }

    @Test
    public void testOnBindClient() throws Exception {
        //given
        final Pointer resourcePointer = mock(Pointer.class);
        when(this.waylandServerLibraryMapping.wl_resource_create(any(),
                                                                 any(),
                                                                 anyInt(),
                                                                 anyInt())).thenReturn(resourcePointer);

        final WlSubCompositor wlSubCompositor = new WlSubCompositor(this.display,
                                                                    this.wlSubSurfaceFactory);
        //when
        final WlSubcompositorResource wlSubcompositorResource = wlSubCompositor.onBindClient(mock(Client.class),
                                                                                             1,
                                                                                             1);
        //then
        assertThat(wlSubcompositorResource).isNotNull();
        assertThat(wlSubcompositorResource.getImplementation()).isSameAs(wlSubCompositor);
    }
}