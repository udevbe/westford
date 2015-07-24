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
import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.DestroyListener;
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
                                                           this.finiteRegionFactory,
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
        final LinkedList<WlSurfaceResource> surfacesStack      = new LinkedList<>();
        final WlSurfaceResource             wlSurfaceResource0 = mock(WlSurfaceResource.class);
        surfacesStack.add(wlSurfaceResource0);
        when(this.compositor.getSurfacesStack()).thenReturn(surfacesStack);

        final WlSurface wlSurface = mock(WlSurface.class);
        when(this.wlSurfaceFactory.create(any())).thenReturn(wlSurface);

        final WlSurfaceResource wlSurfaceResource1 = mock(WlSurfaceResource.class);
        when(wlSurface.add(any(),
                           anyInt(),
                           anyInt())).thenReturn(wlSurfaceResource1);

        final WlCompositorResource wlCompositorResource = mock(WlCompositorResource.class);
        final Client               client               = mock(Client.class);
        when(wlCompositorResource.getClient()).thenReturn(client);
        final int version = 3;
        when(wlCompositorResource.getVersion()).thenReturn(version);

        final Surface surface = mock(Surface.class);
        when(this.surfaceFactory.create(wlCompositorResource)).thenReturn(surface);

        final WlCompositor wlCompositor = new WlCompositor(this.display,
                                                           this.wlSurfaceFactory,
                                                           this.wlRegionFactory,
                                                           this.finiteRegionFactory,
                                                           this.surfaceFactory,
                                                           this.compositor);
        //when
        final int id = 1;
        wlCompositor.createSurface(wlCompositorResource,
                                   1);
        //then
        verify(wlSurface).add(client,
                              version,
                              id);
        assertThat((Iterable<WlSurfaceResource>) surfacesStack).containsExactly(wlSurfaceResource0,
                                                                                wlSurfaceResource1)
                                                               .inOrder();

        final ArgumentCaptor<DestroyListener> destroyListenerCaptor = ArgumentCaptor.forClass(DestroyListener.class);
        verify(wlSurfaceResource1).register(destroyListenerCaptor.capture());
        final DestroyListener destroyListener = destroyListenerCaptor.getValue();

        //and later when
        destroyListener.handle();

        //then
        assertThat((Iterable) surfacesStack).doesNotContain(wlSurfaceResource1);
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

        final WlCompositor wlCompositor = new WlCompositor(this.display,
                                                           this.wlSurfaceFactory,
                                                           this.wlRegionFactory,
                                                           this.finiteRegionFactory,
                                                           this.surfaceFactory,
                                                           this.compositor);
        //when
        final int id = 5;
        wlCompositor.createRegion(wlCompositorResource,
                                  id);
        //then
        verify(wlRegion).add(client,
                             version,
                             id);
    }

    @Test
    public void testCreate() throws Exception {
        //given
        final WlCompositor wlCompositor = new WlCompositor(this.display,
                                                           this.wlSurfaceFactory,
                                                           this.wlRegionFactory,
                                                           this.finiteRegionFactory,
                                                           this.surfaceFactory,
                                                           this.compositor);
        final Client client  = mock(Client.class);
        final int    version = 1;
        final int    id      = 6;
        //when
        final WlCompositorResource wlCompositorResource = wlCompositor.create(client,
                                                                              version,
                                                                              id);
        //then
        assertThat(wlCompositorResource).isNotNull();
    }
}