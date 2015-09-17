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

import com.sun.jna.Pointer;
import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.DestroyListener;
import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.WlCompositorResource;
import org.freedesktop.wayland.server.WlSurfaceResource;
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
import org.westmalle.wayland.core.Compositor;
import org.westmalle.wayland.core.FiniteRegion;
import org.westmalle.wayland.core.FiniteRegionFactory;
import org.westmalle.wayland.core.Surface;
import org.westmalle.wayland.core.SurfaceFactory;
import org.westmalle.wayland.core.SurfaceState;
import org.westmalle.wayland.core.events.Signal;
import org.westmalle.wayland.core.events.Slot;

import java.util.LinkedList;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
                        //following classes have static methods, so we have to powermock them:
                        WaylandServerLibrary.class,
                        InterfaceMeta.class,
                        //following classes are final, so we have to powermock them:
                        WlSurfaceFactory.class,
                        WlRegionFactory.class,
                        FiniteRegionFactory.class,
                        SurfaceFactory.class
                })
public class WlCompositorTest {

    @Mock
    private Display             display;
    @Mock
    private WlSurfaceFactory    wlSurfaceFactory;
    @Mock
    private WlRegionFactory     wlRegionFactory;
    @Mock
    private FiniteRegionFactory finiteRegionFactory;
    @Mock
    private SurfaceFactory      surfaceFactory;
    @Mock
    private Compositor          compositor;

    @Mock
    private WaylandServerLibraryMapping waylandServerLibraryMapping;
    @Mock
    private InterfaceMeta               interfaceMeta;
    @Mock
    private Pointer                     globalPointer;

    private WlCompositor wlCompositor;

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
        this.wlCompositor = new WlCompositor(this.display,
                                             this.wlSurfaceFactory,
                                             this.wlRegionFactory,
                                             this.finiteRegionFactory,
                                             this.surfaceFactory,
                                             this.compositor);
    }

    @Test
    public void testOnBindClient() throws Exception {
        //given
        final Pointer resourcePointer = mock(Pointer.class);
        when(this.waylandServerLibraryMapping.wl_resource_create(any(),
                                                                 any(),
                                                                 anyInt(),
                                                                 anyInt())).thenReturn(resourcePointer);

        //when
        final WlCompositorResource wlCompositorResource = this.wlCompositor.onBindClient(mock(Client.class),
                                                                                         1,
                                                                                         1);
        //then
        assertThat(wlCompositorResource).isNotNull();
        assertThat(wlCompositorResource.getImplementation()).isSameAs(this.wlCompositor);
    }

    @Test
    public void testCreateSurface() throws Exception {
        //given
        final LinkedList<WlSurfaceResource> surfacesStack = new LinkedList<>();
        when(this.compositor.getSurfacesStack()).thenReturn(surfacesStack);

        final WlSurfaceResource wlSurfaceResource0 = mock(WlSurfaceResource.class);
        final WlSurface         wlSurface0         = mock(WlSurface.class);
        when(wlSurface0.add(any(),
                            anyInt(),
                            anyInt())).thenReturn(wlSurfaceResource0);
        when(wlSurfaceResource0.getImplementation()).thenReturn(wlSurface0);
        final Surface surface0 = mock(Surface.class);
        when(wlSurface0.getSurface()).thenReturn(surface0);
        final Signal<SurfaceState, Slot<SurfaceState>> commitSignal0 = mock(Signal.class);
        when(surface0.getCommitSignal()).thenReturn(commitSignal0);

        final WlSurfaceResource wlSurfaceResource1 = mock(WlSurfaceResource.class);
        final WlSurface         wlSurface1         = mock(WlSurface.class);
        when(wlSurface1.add(any(),
                            anyInt(),
                            anyInt())).thenReturn(wlSurfaceResource1);
        when(wlSurfaceResource1.getImplementation()).thenReturn(wlSurface1);
        final Surface surface1 = mock(Surface.class);
        when(wlSurface1.getSurface()).thenReturn(surface1);
        final Signal<SurfaceState, Slot<SurfaceState>> commitSignal1 = mock(Signal.class);
        when(surface1.getCommitSignal()).thenReturn(commitSignal1);

        final WlCompositorResource wlCompositorResource = mock(WlCompositorResource.class);
        final Client               client               = mock(Client.class);
        when(wlCompositorResource.getClient()).thenReturn(client);
        final int version = 3;
        when(wlCompositorResource.getVersion()).thenReturn(version);

        when(this.surfaceFactory.create(wlCompositorResource)).thenReturn(surface0,
                                                                          surface1);

        when(this.wlSurfaceFactory.create(any())).thenReturn(wlSurface0,
                                                             wlSurface1);
        //when
        final int id0 = 1;
        final int id1 = 5;

        this.wlCompositor.createSurface(wlCompositorResource,
                                        id0);
        this.wlCompositor.createSurface(wlCompositorResource,
                                        id1);
        //then
        verify(wlSurface0).add(client,
                               version,
                               id0);
        verify(wlSurface1).add(client,
                               version,
                               id1);
        assertThat((Iterable<WlSurfaceResource>) surfacesStack).containsExactly(wlSurfaceResource0,
                                                                                wlSurfaceResource1)
                                                               .inOrder();

        final ArgumentCaptor<DestroyListener> destroyListenerCaptor = ArgumentCaptor.forClass(DestroyListener.class);
        verify(wlSurfaceResource1).register(destroyListenerCaptor.capture());
        final DestroyListener destroyListener = destroyListenerCaptor.getValue();

        //and later when
        destroyListener.handle();

        //then
        //TODO check subsurface stacks
        assertThat((Iterable<WlSurfaceResource>) surfacesStack).doesNotContain(wlSurfaceResource1);
        verify(this.compositor).requestRender();
    }

    @Test
    public void testCreateRegion() throws Exception {
        //given
        final FiniteRegion region = mock(FiniteRegion.class);
        when(this.finiteRegionFactory.create()).thenReturn(region);

        final WlRegion wlRegion = mock(WlRegion.class);
        when(this.wlRegionFactory.create(any())).thenReturn(wlRegion);

        final WlCompositorResource wlCompositorResource = mock(WlCompositorResource.class);
        final Client               client               = mock(Client.class);
        when(wlCompositorResource.getClient()).thenReturn(client);
        final int version = 2;
        when(wlCompositorResource.getVersion()).thenReturn(version);
        //when
        final int id = 5;
        this.wlCompositor.createRegion(wlCompositorResource,
                                       id);
        //then
        verify(wlRegion).add(client,
                             version,
                             id);
    }

    @Test
    public void testCreate() throws Exception {
        //given
        final Client client  = mock(Client.class);
        final int    version = 1;
        final int    id      = 6;
        //when
        final WlCompositorResource wlCompositorResource = this.wlCompositor.create(client,
                                                                                   version,
                                                                                   id);
        //then
        assertThat(wlCompositorResource).isNotNull();
    }
}