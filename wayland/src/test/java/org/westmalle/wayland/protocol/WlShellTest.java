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
import org.freedesktop.wayland.server.WlShellResource;
import org.freedesktop.wayland.server.WlShellSurfaceResource;
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
import org.westmalle.wayland.core.Role;
import org.westmalle.wayland.core.Surface;
import org.westmalle.wayland.wlshell.ShellSurface;
import org.westmalle.wayland.wlshell.ShellSurfaceFactory;

import java.util.Optional;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
                        //following classes have static methods, so we have to powermock them:
                        WaylandServerLibrary.class,
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
    public void testGetShellSurfacePreviousNonShellSurfaceRole() throws Exception {
        //given
        final WlShellResource   wlShellResource   = mock(WlShellResource.class);
        final int               id                = 123;
        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
        final WlSurface         wlSurface         = mock(WlSurface.class);
        final Surface           surface           = mock(Surface.class);
        final Role              role              = mock(Role.class);
        final Optional<Role>    roleOptional      = Optional.of(role);

        final Client client  = mock(Client.class);
        final int    version = 3;

        when(wlShellResource.getClient()).thenReturn(client);
        when(wlShellResource.getVersion()).thenReturn(version);

        when(wlSurfaceResource.getImplementation()).thenReturn(wlSurface);
        when(wlSurface.getSurface()).thenReturn(surface);
        when(surface.getRole()).thenReturn(roleOptional);

        final ShellSurfaceFactory shellSurfaceFactory = mock(ShellSurfaceFactory.class);
        final WlCompositor        wlCompositor        = mock(WlCompositor.class);

        final WlShell wlShell = new WlShell(this.display,
                                            this.wlShellSurfaceFactory,
                                            shellSurfaceFactory,
                                            wlCompositor);

        //when
        wlShell.getShellSurface(wlShellResource,
                                id,
                                wlSurfaceResource);

        //then
        verifyZeroInteractions(shellSurfaceFactory);
        verifyZeroInteractions(this.wlShellSurfaceFactory);
        //TODO verify protocol error is send out
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

        final ShellSurfaceFactory shellSurfaceFactory = mock(ShellSurfaceFactory.class);
        final WlCompositor        wlCompositor        = mock(WlCompositor.class);
        when(shellSurfaceFactory.create(eq(wlCompositor),
                                        anyInt())).thenReturn(shellSurface);

        final WlShell wlShell = new WlShell(this.display,
                                            this.wlShellSurfaceFactory,
                                            shellSurfaceFactory,
                                            wlCompositor);
        //when
        wlShell.getShellSurface(wlShellResource,
                                id,
                                wlSurfaceResource);
        //then
        verify(wlShellSurface).add(client,
                                   version,
                                   id);
        verify(surface).setRole(shellSurface);

        final ArgumentCaptor<DestroyListener> destroyListenerArgumentCaptor = ArgumentCaptor.forClass(DestroyListener.class);
        verify(wlSurfaceResource).register(destroyListenerArgumentCaptor.capture());

        //and when
        final DestroyListener destroyListener = destroyListenerArgumentCaptor.getValue();
        destroyListener.handle();
        //then
        verify(wlShellSurfaceResource).destroy();

        //and when
        wlShell.getShellSurface(wlShellResource,
                                id,
                                wlSurfaceResource);

        //then
        verify(wlShellSurface).add(client,
                                   version,
                                   id);
    }

    @Test
    public void testOnBindClient() throws Exception {
        //given
        final Pointer resourcePointer = mock(Pointer.class);
        when(this.waylandServerLibraryMapping.wl_resource_create(any(),
                                                                 any(),
                                                                 anyInt(),
                                                                 anyInt())).thenReturn(resourcePointer);
        final ShellSurfaceFactory shellSurfaceFactory = mock(ShellSurfaceFactory.class);
        final WlCompositor        wlCompositor        = mock(WlCompositor.class);
        final WlShell wlShell = new WlShell(this.display,
                                            this.wlShellSurfaceFactory,
                                            shellSurfaceFactory,
                                            wlCompositor);
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
        final Client              client              = mock(Client.class);
        final int                 version             = 2;
        final int                 id                  = 7;
        final ShellSurfaceFactory shellSurfaceFactory = mock(ShellSurfaceFactory.class);
        final WlCompositor        wlCompositor        = mock(WlCompositor.class);
        final WlShell wlShell = new WlShell(this.display,
                                            this.wlShellSurfaceFactory,
                                            shellSurfaceFactory,
                                            wlCompositor);
        //when
        final WlShellResource wlShellResource = wlShell.create(client,
                                                               version,
                                                               id);
        //then
        assertThat(wlShellResource).isNotNull();
        assertThat(wlShellResource.getImplementation()).isSameAs(wlShell);
    }
}