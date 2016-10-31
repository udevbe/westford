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

import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.WlPointerResource;
import org.freedesktop.wayland.server.WlSeatResource;
import org.freedesktop.wayland.server.WlShellSurfaceResource;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.freedesktop.wayland.server.jaccall.WaylandServerCore;
import org.freedesktop.wayland.shared.WlShellSurfaceTransient;
import org.freedesktop.wayland.util.ObjectCache;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.westford.compositor.wlshell.ShellSurface;

import java.util.EnumSet;
import java.util.Optional;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(WaylandServerCore.class)
public class WlShellSurfaceTest {

    @Mock
    private ShellSurface      shellSurface;
    @Mock
    private WlSurfaceResource wlSurfaceResource;
    @Mock
    private WaylandServerCore waylandServerCore;

    private WlShellSurface wlShellSurface;

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(WaylandServerCore.class);
        when(WaylandServerCore.INSTANCE()).thenReturn(this.waylandServerCore);
        ObjectCache.remove(112358L);
        when(this.waylandServerCore.wl_resource_create(anyLong(),
                                                       anyLong(),
                                                       anyInt(),
                                                       anyInt())).thenReturn(112358L);
        this.wlShellSurface = new WlShellSurface(this.shellSurface,
                                                 this.wlSurfaceResource);
    }

    @Test
    public void testMove() throws Exception {
        //given
        final WlSeatResource wlSeatResource = mock(WlSeatResource.class);
        final WlSeat         wlSeat         = mock(WlSeat.class);
        when(wlSeatResource.getImplementation()).thenReturn(wlSeat);

        final WlPointer         wlPointer         = mock(WlPointer.class);
        final WlPointerResource wlPointerResource = mock(WlPointerResource.class);
        when(wlPointerResource.getImplementation()).thenReturn(wlPointer);
        when(wlSeat.getWlPointerResource(wlSeatResource)).thenReturn(Optional.of(wlPointerResource));

        final WlShellSurfaceResource wlShellSurfaceResource = mock(WlShellSurfaceResource.class);

        final int serial = 454;

        //when
        this.wlShellSurface.move(wlShellSurfaceResource,
                                 wlSeatResource,
                                 serial);
        //then
        verify(this.shellSurface).move(this.wlSurfaceResource,
                                       wlPointerResource,
                                       serial);
    }

    @Test
    public void testCreate() throws Exception {
        //given
        final Client client = mock(Client.class);
        Whitebox.setInternalState(client,
                                  "pointer",
                                  2468L);
        final int version = 1;
        final int id      = 1;

        //when
        final WlShellSurfaceResource wlShellSurfaceResource = this.wlShellSurface.create(client,
                                                                                         version,
                                                                                         id);
        //then
        assertThat(wlShellSurfaceResource).isNotNull();
    }

    @Test
    public void testResize() throws Exception {
        //given
        final WlSeatResource wlSeatResource = mock(WlSeatResource.class);
        final WlSeat         wlSeat         = mock(WlSeat.class);
        when(wlSeatResource.getImplementation()).thenReturn(wlSeat);

        final WlPointer         wlPointer         = mock(WlPointer.class);
        final WlPointerResource wlPointerResource = mock(WlPointerResource.class);
        when(wlPointerResource.getImplementation()).thenReturn(wlPointer);
        when(wlSeat.getWlPointerResource(wlSeatResource)).thenReturn(Optional.of(wlPointerResource));

        final WlShellSurfaceResource wlShellSurfaceResource = mock(WlShellSurfaceResource.class);

        final int serial = 454;
        final int edges  = 8;

        //when
        this.wlShellSurface.resize(wlShellSurfaceResource,
                                   wlSeatResource,
                                   serial,
                                   edges);
        //then
        verify(this.shellSurface).resize(wlShellSurfaceResource,
                                         this.wlSurfaceResource,
                                         wlPointerResource,
                                         serial,
                                         edges);
    }

    @Test
    public void testPong() {
        //given
        final WlShellSurfaceResource wlShellSurfaceResource = mock(WlShellSurfaceResource.class);
        final int                    serial                 = 7654;

        //when
        this.wlShellSurface.pong(wlShellSurfaceResource,
                                 serial);

        //then
        verify(this.shellSurface).pong(wlShellSurfaceResource,
                                       serial);
    }

    @Test
    public void testSetTitle() {
        //given
        final WlShellSurfaceResource wlShellSurfaceResource = mock(WlShellSurfaceResource.class);
        final String                 title                  = "appTitle";

        //when
        this.wlShellSurface.setTitle(wlShellSurfaceResource,
                                     title);

        //then
        verify(this.shellSurface).setTitle(Optional.of(title));
    }

    @Test
    public void testSetClass() {
        //given
        final WlShellSurfaceResource wlShellSurfaceResource = mock(WlShellSurfaceResource.class);
        final String                 clazz                  = "appClass";

        //when
        this.wlShellSurface.setClass(wlShellSurfaceResource,
                                     clazz);

        //then
        verify(this.shellSurface).setClazz(Optional.of(clazz));
    }

    @Test
    public void testSetToplevel() throws Exception {
        //given
        final WlShellSurfaceResource wlShellSurfaceResource = mock(WlShellSurfaceResource.class);

        //when
        this.wlShellSurface.setToplevel(wlShellSurfaceResource);

        //then
        verify(this.shellSurface).toFront(this.wlSurfaceResource);
    }

    @Test
    public void testSetTransient() throws Exception {
        //given
        final WlShellSurfaceResource wlShellSurfaceResource  = mock(WlShellSurfaceResource.class);
        final WlSurfaceResource      parentWlSurfaceResource = mock(WlSurfaceResource.class);
        final int                    x                       = 1235;
        final int                    y                       = 9876;
        final int                    flags                   = WlShellSurfaceTransient.INACTIVE.value;

        //when
        this.wlShellSurface.setTransient(wlShellSurfaceResource,
                                         parentWlSurfaceResource,
                                         x,
                                         y,
                                         flags);

        //then
        verify(this.shellSurface).setTransient(this.wlSurfaceResource,
                                               parentWlSurfaceResource,
                                               x,
                                               y,
                                               EnumSet.of(WlShellSurfaceTransient.INACTIVE));
    }
}