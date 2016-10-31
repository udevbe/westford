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
package org.westford.compositor.protocol;

import org.freedesktop.jaccall.Pointer;
import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.DestroyListener;
import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.Resource;
import org.freedesktop.wayland.server.WlShellResource;
import org.freedesktop.wayland.server.WlShellSurfaceResource;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.freedesktop.wayland.server.jaccall.WaylandServerCore;
import org.freedesktop.wayland.shared.WlShellError;
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
import org.westford.compositor.core.Role;
import org.westford.compositor.core.Surface;
import org.westford.compositor.wlshell.ShellSurface;
import org.westford.compositor.wlshell.ShellSurfaceFactory;

import java.util.Optional;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
                        //following classes have static methods, so we have to powermock them:
                        WaylandServerCore.class,
                        InterfaceMeta.class,
                        //following classes are final, so we have to powermock them:
                        WlShellSurfaceFactory.class,
                        ShellSurfaceFactory.class
                })
public class WlShellTest {

    @Mock
    private Display               display;
    @Mock
    private WlShellSurfaceFactory wlShellSurfaceFactory;

    @Mock
    private WaylandServerCore   waylandServerCore;
    @Mock
    private InterfaceMeta       interfaceMeta;
    @Mock
    private ShellSurfaceFactory shellSurfaceFactory;

    private WlShell wlShell;

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
        this.wlShell = new WlShell(this.display,
                                   this.wlShellSurfaceFactory,
                                   this.shellSurfaceFactory);
    }

    @Test
    public void testGetShellSurfacePreviousNonShellSurfaceRole() throws Exception {
        //given
        final WlShellResource   wlShellResource   = mock(WlShellResource.class);
        final int               id                = 123;
        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
        final WlSurface         wlSurface         = mock(WlSurface.class);
        final Surface           surface           = mock(Surface.class);
        final Role              role              = mock(Role.class);
        final Optional<Role>    roleOptional      = Optional.of(role);
        final Resource          displayResource   = mock(Resource.class);

        final Client client  = mock(Client.class);
        final int    version = 3;

        when(client.getObject(Display.OBJECT_ID)).thenReturn(displayResource);

        when(wlShellResource.getClient()).thenReturn(client);
        when(wlShellResource.getVersion()).thenReturn(version);

        when(wlSurfaceResource.getImplementation()).thenReturn(wlSurface);
        when(wlSurface.getSurface()).thenReturn(surface);
        when(surface.getRole()).thenReturn(roleOptional);

        //when
        this.wlShell.getShellSurface(wlShellResource,
                                     id,
                                     wlSurfaceResource);

        //then
        verifyZeroInteractions(this.shellSurfaceFactory);
        verifyZeroInteractions(this.wlShellSurfaceFactory);
        verify(displayResource).postError(eq(WlShellError.ROLE.value),
                                          anyString());
    }

    @Test
    public void testGetShellSurfaceNoPreviousRole() throws Exception {
        //given
        final WlShellResource   wlShellResource   = mock(WlShellResource.class);
        final int               id                = 123;
        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
        final WlSurface         wlSurface         = mock(WlSurface.class);
        final Surface           surface           = mock(Surface.class);
        final Optional<Role>    roleOptional      = Optional.empty();

        final Client client  = mock(Client.class);
        final int    version = 3;

        when(wlShellResource.getClient()).thenReturn(client);
        when(wlShellResource.getVersion()).thenReturn(version);

        when(wlSurfaceResource.getImplementation()).thenReturn(wlSurface);
        when(wlSurface.getSurface()).thenReturn(surface);
        when(surface.getRole()).thenReturn(roleOptional);

        final WlShellSurface wlShellSurface = mock(WlShellSurface.class);
        final ShellSurface   shellSurface   = mock(ShellSurface.class);
        when(wlShellSurface.getShellSurface()).thenReturn(shellSurface);
        when(this.wlShellSurfaceFactory.create(shellSurface,
                                               wlSurfaceResource)).thenReturn(wlShellSurface);

        final WlShellSurfaceResource wlShellSurfaceResource = mock(WlShellSurfaceResource.class);
        when(wlShellSurface.add(any(),
                                anyInt(),
                                anyInt())).thenReturn(wlShellSurfaceResource);
        when(this.shellSurfaceFactory.create(anyInt())).thenReturn(shellSurface);
        //when
        this.wlShell.getShellSurface(wlShellResource,
                                     id,
                                     wlSurfaceResource);
        //then
        verify(wlShellSurface).add(client,
                                   version,
                                   id);
        verify(surface).setRole(shellSurface);

        final ArgumentCaptor<DestroyListener> surfaceResourceDestroyListenerCaptor      = ArgumentCaptor.forClass(DestroyListener.class);
        final ArgumentCaptor<DestroyListener> shellSurfaceResourceDestroyListenerCaptor = ArgumentCaptor.forClass(DestroyListener.class);

        verify(wlSurfaceResource).register(surfaceResourceDestroyListenerCaptor.capture());
        verify(wlShellSurfaceResource).register(shellSurfaceResourceDestroyListenerCaptor.capture());

        //and when
        final DestroyListener surfaceDestroyListener = surfaceResourceDestroyListenerCaptor.getValue();
        surfaceDestroyListener.handle();
        final DestroyListener shellSurfaceDestroyListener = shellSurfaceResourceDestroyListenerCaptor.getValue();
        shellSurfaceDestroyListener.handle();

        //then
        verify(wlShellSurfaceResource).destroy();

        //and when
        this.wlShell.getShellSurface(wlShellResource,
                                     id,
                                     wlSurfaceResource);

        //then
        verify(wlShellSurface,
               times(2)).add(client,
                             version,
                             id);
    }

    @Test
    public void testGetShellSurfacePreviousRole() throws Exception {
        //given
        final WlShellResource   wlShellResource   = mock(WlShellResource.class);
        final int               id                = 123;
        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
        final WlSurface         wlSurface         = mock(WlSurface.class);
        final Surface           surface           = mock(Surface.class);
        final Optional<Role>    roleOptional      = Optional.of(mock(Role.class));
        final Client            client            = mock(Client.class);
        final Resource          displayResource   = mock(Resource.class);

        when(wlShellResource.getClient()).thenReturn(client);
        when(client.getObject(Display.OBJECT_ID)).thenReturn(displayResource);

        when(wlSurfaceResource.getImplementation()).thenReturn(wlSurface);
        when(wlSurface.getSurface()).thenReturn(surface);
        when(surface.getRole()).thenReturn(roleOptional);

        //when
        this.wlShell.getShellSurface(wlShellResource,
                                     id,
                                     wlSurfaceResource);
        //then
        verify(displayResource).postError(eq(WlShellError.ROLE.value),
                                          anyString());
    }

    @Test
    public void testOnBindClient() throws Exception {
        //given
        final Client client = mock(Client.class);
        Whitebox.setInternalState(client,
                                  "pointer",
                                  2468L);
        //when
        final WlShellResource wlShellResource = this.wlShell.onBindClient(client,
                                                                          1,
                                                                          1);
        //then
        assertThat(wlShellResource).isNotNull();
        assertThat(wlShellResource.getImplementation()).isSameAs(this.wlShell);
    }

    @Test
    public void testCreate() throws Exception {
        //given
        final Client client = mock(Client.class);
        Whitebox.setInternalState(client,
                                  "pointer",
                                  2468L);
        final int version = 2;
        final int id      = 7;
        //when
        final WlShellResource wlShellResource = this.wlShell.create(client,
                                                                    version,
                                                                    id);
        //then
        assertThat(wlShellResource).isNotNull();
        assertThat(wlShellResource.getImplementation()).isSameAs(this.wlShell);
    }
}