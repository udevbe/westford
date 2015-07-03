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

import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.WlSeatResource;
import org.freedesktop.wayland.server.WlShellSurfaceResource;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.freedesktop.wayland.server.jna.WaylandServerLibrary;
import org.freedesktop.wayland.server.jna.WaylandServerLibraryMapping;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.westmalle.wayland.wlshell.ShellSurface;

import java.util.Optional;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(WaylandServerLibrary.class)
public class WlShellSurfaceTest {

    @Mock
    private ShellSurface                shellSurface;
    @Mock
    private WaylandServerLibraryMapping waylandServerLibraryMapping;

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(WaylandServerLibrary.class);
        when(WaylandServerLibrary.INSTANCE()).thenReturn(this.waylandServerLibraryMapping);
    }

    @Test
    public void testMove() throws Exception {
        //given
        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
        final WlShellSurface wlShellSurface = new WlShellSurface(this.shellSurface,
                                                                 wlSurfaceResource);

        final WlSeatResource wlSeatResource = mock(WlSeatResource.class);
        final WlSeat         wlSeat         = mock(WlSeat.class);
        when(wlSeatResource.getImplementation()).thenReturn(wlSeat);

        final WlPointer wlPointer = mock(WlPointer.class);
        when(wlSeat.getOptionalWlPointer()).thenReturn(Optional.of(wlPointer));

        final WlShellSurfaceResource wlShellSurfaceResource = mock(WlShellSurfaceResource.class);

        final int serial = 454;

        //when
        wlShellSurface.move(wlShellSurfaceResource,
                            wlSeatResource,
                            serial);
        //then
        verify(this.shellSurface).move(wlSurfaceResource,
                                       wlPointer,
                                       serial);
    }

    @Test
    public void testCreate() throws Exception {
        //given
        final Client client  = mock(Client.class);
        final int    version = 1;
        final int    id      = 1;

        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
        final WlShellSurface wlShellSurface = new WlShellSurface(this.shellSurface,
                                                                 wlSurfaceResource);

        //when
        final WlShellSurfaceResource wlShellSurfaceResource = wlShellSurface.create(client,
                                                                                    version,
                                                                                    id);
        //then
        assertThat(wlShellSurfaceResource).isNotNull();
    }

    @Test
    public void testResize() throws Exception {
        //given
        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
        final WlShellSurface wlShellSurface = new WlShellSurface(this.shellSurface,
                                                                 wlSurfaceResource);

        final WlSeatResource wlSeatResource = mock(WlSeatResource.class);
        final WlSeat         wlSeat         = mock(WlSeat.class);
        when(wlSeatResource.getImplementation()).thenReturn(wlSeat);

        final WlPointer wlPointer = mock(WlPointer.class);
        when(wlSeat.getOptionalWlPointer()).thenReturn(Optional.of(wlPointer));

        final WlShellSurfaceResource wlShellSurfaceResource = mock(WlShellSurfaceResource.class);

        final int serial = 454;
        final int edges  = 8;

        //when
        wlShellSurface.resize(wlShellSurfaceResource,
                              wlSeatResource,
                              serial,
                              edges);
        //then
        verify(this.shellSurface).resize(wlShellSurfaceResource,
                                         wlSurfaceResource,
                                         wlPointer,
                                         serial,
                                         edges);
    }

    @Test
    public void testPong() {
        //given
        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
        final WlShellSurface wlShellSurface = new WlShellSurface(this.shellSurface,
                                                                 wlSurfaceResource);

        final WlShellSurfaceResource wlShellSurfaceResource = mock(WlShellSurfaceResource.class);
        final int                    serial                 = 7654;

        //when
        wlShellSurface.pong(wlShellSurfaceResource,
                            serial);

        //then
        verify(this.shellSurface).pong(wlShellSurfaceResource,
                                       serial);
    }

    @Test
    public void testSetTitle() {
        //given
        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
        final WlShellSurface wlShellSurface = new WlShellSurface(this.shellSurface,
                                                                 wlSurfaceResource);

        final WlShellSurfaceResource wlShellSurfaceResource = mock(WlShellSurfaceResource.class);
        final String                 title                  = "appTitle";

        //when
        wlShellSurface.setTitle(wlShellSurfaceResource,
                                title);

        //then
        verify(this.shellSurface).setTitle(Optional.of(title));
    }

    @Test
    public void testSetClass() {
        //given
        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
        final WlShellSurface wlShellSurface = new WlShellSurface(this.shellSurface,
                                                                 wlSurfaceResource);

        final WlShellSurfaceResource wlShellSurfaceResource = mock(WlShellSurfaceResource.class);
        final String                 clazz                  = "appClass";

        //when
        wlShellSurface.setClass(wlShellSurfaceResource,
                                clazz);

        //then
        verify(this.shellSurface).setClazz(Optional.of(clazz));
    }

    @Test
    public void testSetToplevel() throws Exception {
        //given
        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
        final WlShellSurface wlShellSurface = new WlShellSurface(this.shellSurface,
                                                                 wlSurfaceResource);

        final WlShellSurfaceResource wlShellSurfaceResource = mock(WlShellSurfaceResource.class);

        //when
        wlShellSurface.setToplevel(wlShellSurfaceResource);

        //then
        verify(this.shellSurface).toFront(wlSurfaceResource);
    }

    @Test
    public void testSetTransient() throws Exception {
        //given
        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
        final WlShellSurface wlShellSurface = new WlShellSurface(this.shellSurface,
                                                                 wlSurfaceResource);

        final WlShellSurfaceResource wlShellSurfaceResource  = mock(WlShellSurfaceResource.class);
        final WlSurfaceResource      parentWlSurfaceResource = mock(WlSurfaceResource.class);
        final int                    x                       = 1235;
        final int                    y                       = 9876;
        final int                    flags                   = 0;

        //when
        wlShellSurface.setTransient(wlShellSurfaceResource,
                                    parentWlSurfaceResource,
                                    x,
                                    y,
                                    flags);

        //then
        verify(this.shellSurface).setTransient(wlSurfaceResource,
                                               parentWlSurfaceResource,
                                               x,
                                               y,
                                               flags);
    }
}