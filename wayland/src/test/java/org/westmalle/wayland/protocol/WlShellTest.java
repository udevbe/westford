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
                        WlShellSurfaceFactory.class
                })
public class WlShellTest {

    @Mock
    private Display               display;
    @Mock
    private WlShellSurfaceFactory wlShellSurfaceFactory;

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
    public void testGetShellSurface() throws Exception {
        //given
        final WlShellResource wlShellResource = mock(WlShellResource.class);
        final int id = 123;
        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
        final Client client = mock(Client.class);
        final int version = 3;
        when(wlShellResource.getClient()).thenReturn(client);
        when(wlShellResource.getVersion()).thenReturn(version);

        final WlShellSurface wlShellSurface = mock(WlShellSurface.class);
        when(this.wlShellSurfaceFactory.create(wlSurfaceResource)).thenReturn(wlShellSurface);

        final WlShellSurfaceResource wlShellSurfaceResource = mock(WlShellSurfaceResource.class);
        when(wlShellSurface.add(any(),
                                anyInt(),
                                anyInt())).thenReturn(wlShellSurfaceResource);

        final WlShell wlShell = new WlShell(this.display,
                                            this.wlShellSurfaceFactory);
        //when
        wlShell.getShellSurface(wlShellResource,
                                id,
                                wlSurfaceResource);
        //then
        verify(wlShellSurface,
               times(1)).add(client,
                             version,
                             id);
        ArgumentCaptor<Listener> destroyListenerArgumentCaptor = ArgumentCaptor.forClass(Listener.class);
        verify(wlSurfaceResource,
               times(1)).addDestroyListener(destroyListenerArgumentCaptor.capture());

        //and when
        final Listener destroyListener = destroyListenerArgumentCaptor.getValue();
        destroyListener.handle();
        //then
        verify(wlShellSurfaceResource,
               times(1)).destroy();
    }

    @Test
    public void testOnBindClient() throws Exception {
        //given
        final Pointer resourcePointer = mock(Pointer.class);
        when(this.waylandServerLibraryMapping.wl_resource_create(any(),
                                                                 any(),
                                                                 anyInt(),
                                                                 anyInt())).thenReturn(resourcePointer);
        final WlShell wlShell = new WlShell(this.display,
                                            this.wlShellSurfaceFactory);
        //when
        final WlShellResource wlShellResource = wlShell.onBindClient(mock(Client.class),
                                                                     1,
                                                                     1);
        //then
        assertThat(wlShellResource).isNotNull();
        assertThat(wlShellResource.getImplementation()).isSameAs(wlShell);
    }

    @Test
    public void testCreate() throws Exception {
        //given
        final Client client = mock(Client.class);
        final int version = 2;
        final int id = 7;
        final WlShell wlShell = new WlShell(this.display,
                                            this.wlShellSurfaceFactory);
        //when
        final WlShellResource wlShellResource = wlShell.create(client,
                                                               version,
                                                               id);
        //then
        assertThat(wlShellResource).isNotNull();
        assertThat(wlShellResource.getImplementation()).isSameAs(wlShell);
    }
}