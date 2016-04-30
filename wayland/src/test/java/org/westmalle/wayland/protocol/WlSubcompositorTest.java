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

import org.freedesktop.jaccall.Pointer;
import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.DestroyListener;
import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.Resource;
import org.freedesktop.wayland.server.WlSubcompositorResource;
import org.freedesktop.wayland.server.WlSubsurfaceResource;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.freedesktop.wayland.server.jaccall.WaylandServerCore;
import org.freedesktop.wayland.shared.WlSubcompositorError;
import org.freedesktop.wayland.util.InterfaceMeta;
import org.freedesktop.wayland.util.ObjectCache;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.westmalle.wayland.core.Role;
import org.westmalle.wayland.core.Subsurface;
import org.westmalle.wayland.core.SubsurfaceFactory;
import org.westmalle.wayland.core.Surface;

import java.util.Optional;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
                        //following classes have static methods, so we have to powermock them:
                        WaylandServerCore.class,
                        InterfaceMeta.class,
                        //following classes are final, so we have to powermock them:
                        WlSubsurfaceFactory.class,
                        SubsurfaceFactory.class
                })
public class WlSubcompositorTest {

    @Mock
    private Display             display;
    @Mock
    private WlSubsurfaceFactory wlSubSurfaceFactory;
    @Mock
    private SubsurfaceFactory   subsurfaceFactory;

    @Mock
    private WaylandServerCore waylandServerCore;
    @Mock
    private InterfaceMeta     interfaceMeta;

    private WlSubcompositor wlSubcompositor;

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(WaylandServerCore.class,
                                InterfaceMeta.class);
        when(InterfaceMeta.get((Class<?>) any())).thenReturn(this.interfaceMeta);
        when(this.interfaceMeta.getNative()).thenReturn(mock(Pointer.class));
        when(WaylandServerCore.INSTANCE()).thenReturn(this.waylandServerCore);
        final long globalPointer = 13579L;
        ObjectCache.remove(globalPointer);
        when(this.waylandServerCore.wl_global_create(anyLong(),
                                                     anyLong(),
                                                     anyInt(),
                                                     anyLong(),
                                                     anyLong())).thenReturn(globalPointer);
        ObjectCache.remove(112358L);
        when(this.waylandServerCore.wl_resource_create(anyLong(),
                                                       anyLong(),
                                                       anyInt(),
                                                       anyInt())).thenReturn(112358L);
        Whitebox.setInternalState(this.display,
                                  "pointer",
                                  987654321L);
        this.wlSubcompositor = new WlSubcompositor(this.display,
                                                   this.wlSubSurfaceFactory,
                                                   this.subsurfaceFactory);
    }

    @Test
    public void testDestroy() throws Exception {
        //given
        final WlSubcompositorResource subcompositorResource = mock(WlSubcompositorResource.class);
        //when
        this.wlSubcompositor.destroy(subcompositorResource);
        //then
        verify(subcompositorResource).destroy();
    }

    @Test
    public void testGetSubsurfaceNoPreviousRole() throws Exception {
        //given
        final Client                  client                = mock(Client.class);
        final int                     version               = 4;
        final WlSubcompositorResource subcompositorResource = mock(WlSubcompositorResource.class);
        when(subcompositorResource.getClient()).thenReturn(client);
        when(subcompositorResource.getVersion()).thenReturn(4);

        final int               id                = 123;
        final Subsurface        subsurface        = mock(Subsurface.class);
        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
        final WlSurface         wlSurface         = mock(WlSurface.class);
        final Surface           surface           = mock(Surface.class);
        final Optional<Role>    role              = Optional.empty();
        final WlSurfaceResource parent            = mock(WlSurfaceResource.class);

        when(wlSurfaceResource.getImplementation()).thenReturn(wlSurface);
        when(wlSurface.getSurface()).thenReturn(surface);
        when(surface.getRole()).thenReturn(role);

        when(this.subsurfaceFactory.create(parent,
                                           wlSurfaceResource)).thenReturn(subsurface);

        final WlSubsurface wlSubsurface = mock(WlSubsurface.class);
        when(this.wlSubSurfaceFactory.create(subsurface)).thenReturn(wlSubsurface);

        final WlSubsurfaceResource wlSubsurfaceResource = mock(WlSubsurfaceResource.class);
        when(wlSubsurface.add(any(),
                              anyInt(),
                              anyInt())).thenReturn(wlSubsurfaceResource);
        //when
        this.wlSubcompositor.getSubsurface(subcompositorResource,
                                           id,
                                           wlSurfaceResource,
                                           parent);
        //then
        verify(wlSubsurface).add(client,
                                 version,
                                 id);
        final ArgumentCaptor<DestroyListener> listenerArgumentCaptor = ArgumentCaptor.forClass(DestroyListener.class);
        verify(wlSurfaceResource).register(listenerArgumentCaptor.capture());
        final DestroyListener destroyListener = listenerArgumentCaptor.getValue();
        //and when
        destroyListener.handle();
        //then
        verify(wlSubsurfaceResource).destroy();
    }

    @Test
    public void testGetSubsurfacePreviousRole() throws Exception {
        //given
        final WlSubcompositorResource wlSubcompositorResource = mock(WlSubcompositorResource.class);
        final int                     id                      = 123;
        final WlSurfaceResource       wlSurfaceResource       = mock(WlSurfaceResource.class);
        final WlSurface               wlSurface               = mock(WlSurface.class);
        final Surface                 surface                 = mock(Surface.class);
        final Optional<Role>          roleOptional            = Optional.of(mock(Role.class));
        final Client                  client                  = mock(Client.class);
        final Resource                displayResource         = mock(Resource.class);
        final WlSurfaceResource       parent                  = mock(WlSurfaceResource.class);

        when(wlSubcompositorResource.getClient()).thenReturn(client);
        when(client.getObject(Display.OBJECT_ID)).thenReturn(displayResource);

        when(wlSurfaceResource.getImplementation()).thenReturn(wlSurface);
        when(wlSurface.getSurface()).thenReturn(surface);
        when(surface.getRole()).thenReturn(roleOptional);

        //when
        this.wlSubcompositor.getSubsurface(wlSubcompositorResource,
                                           id,
                                           wlSurfaceResource,
                                           parent);
        //then
        verify(displayResource).postError(eq(WlSubcompositorError.BAD_SURFACE.value),
                                          anyString());

    }

    @Test
    public void testCreate() throws Exception {
        //given
        final Client client = mock(Client.class);
        Whitebox.setInternalState(client,
                                  "pointer",
                                  2468L);
        final int version = 2;
        final int id      = 12;
        //when
        final WlSubcompositorResource wlSubcompositorResource = this.wlSubcompositor.create(client,
                                                                                            version,
                                                                                            id);
        //then
        assertThat(wlSubcompositorResource).isNotNull();
        assertThat(wlSubcompositorResource.getImplementation()).isSameAs(this.wlSubcompositor);
    }

    @Test
    public void testOnBindClient() throws Exception {
        //given
        final Client client = mock(Client.class);
        Whitebox.setInternalState(client,
                                  "pointer",
                                  2468L);
        //when
        final WlSubcompositorResource wlSubcompositorResource = this.wlSubcompositor.onBindClient(client,
                                                                                                  1,
                                                                                                  1);
        //then
        assertThat(wlSubcompositorResource).isNotNull();
        assertThat(wlSubcompositorResource.getImplementation()).isSameAs(this.wlSubcompositor);
    }
}