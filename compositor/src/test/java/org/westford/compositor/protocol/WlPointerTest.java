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
import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.Resource;
import org.freedesktop.wayland.server.WlPointerResource;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.freedesktop.wayland.server.jaccall.WaylandServerCore;
import org.freedesktop.wayland.shared.WlPointerError;
import org.freedesktop.wayland.util.ObjectCache;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.westford.compositor.core.PointerDevice;
import org.westford.compositor.core.Role;
import org.westford.compositor.core.Surface;

import java.util.Optional;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(WaylandServerCore.class)
public class WlPointerTest {

    @Mock
    private PointerDevice     pointerDevice;
    @Mock
    private WaylandServerCore waylandServerCore;

    private WlPointer wlPointer;

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(WaylandServerCore.class);
        when(WaylandServerCore.INSTANCE()).thenReturn(this.waylandServerCore);
        ObjectCache.remove(112358L);
        when(this.waylandServerCore.wl_resource_create(anyLong(),
                                                       anyLong(),
                                                       anyInt(),
                                                       anyInt())).thenReturn(112358L);
        this.wlPointer = new WlPointer(this.pointerDevice);
    }

    @Test
    public void testRelease() throws Exception {
        //given
        final WlPointerResource wlPointerResource = mock(WlPointerResource.class);
        //when
        this.wlPointer.release(wlPointerResource);
        //then
        verify(wlPointerResource).destroy();
    }

    @Test
    public void testCreate() throws Exception {
        //given
        final Client client = mock(Client.class);
        Whitebox.setInternalState(client,
                                  "pointer",
                                  2468L);
        final int       version   = 1;
        final int       id        = 1;
        final WlPointer wlPointer = new WlPointer(this.pointerDevice);
        //when
        final WlPointerResource wlPointerResource = wlPointer.create(client,
                                                                     version,
                                                                     id);
        //then
        assertThat(wlPointerResource).isNotNull();
        assertThat(wlPointerResource.getImplementation()).isSameAs(wlPointer);
    }

    @Test
    public void testSetCursorNullSurface() {
        //given: a pointer with a null surface
        final WlPointerResource wlPointerResource = mock(WlPointerResource.class);
        final int               serial            = 12345;

        //when: SetCursor is called
        this.wlPointer.setCursor(wlPointerResource,
                                 serial,
                                 null,
                                 0,
                                 0);

        //then: cursor is removed on pointer device
        verify(this.pointerDevice).removeCursor(wlPointerResource,
                                                serial);
    }

    @Test
    public void testSetCursor() {
        //given: a pointer with a surface with no role
        final WlPointerResource wlPointerResource = mock(WlPointerResource.class);
        final int               serial            = 12345;
        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
        final WlSurface         wlSurface         = mock(WlSurface.class);
        when(wlSurfaceResource.getImplementation()).thenReturn(wlSurface);
        final Surface surface = mock(Surface.class);
        when(wlSurface.getSurface()).thenReturn(surface);
        when(surface.getRole()).thenReturn(Optional.empty());

        final int hotspotX = 10;
        final int hotspotY = 15;

        //when: SetCursor is called
        this.wlPointer.setCursor(wlPointerResource,
                                 serial,
                                 wlSurfaceResource,
                                 hotspotX,
                                 hotspotY);

        //then: cursor is set on pointer device and role is set for surface
        verify(surface).setRole(this.pointerDevice);
        verify(this.pointerDevice).setCursor(wlPointerResource,
                                             serial,
                                             wlSurfaceResource,
                                             hotspotX,
                                             hotspotY);
    }

    @Test
    public void testSetCursorSameSurface() {
        //given: a pointer with a surface with a role that is this pointer device
        final WlPointerResource wlPointerResource = mock(WlPointerResource.class);
        final int               serial            = 12345;
        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
        final WlSurface         wlSurface         = mock(WlSurface.class);
        when(wlSurfaceResource.getImplementation()).thenReturn(wlSurface);
        final Surface surface = mock(Surface.class);
        when(wlSurface.getSurface()).thenReturn(surface);
        when(surface.getRole()).thenReturn(Optional.of(this.pointerDevice));

        final int hotspotX = 10;
        final int hotspotY = 15;

        //when: SetCursor is called
        this.wlPointer.setCursor(wlPointerResource,
                                 serial,
                                 wlSurfaceResource,
                                 hotspotX,
                                 hotspotY);
        //then: cursor is set on pointer device
        verify(this.pointerDevice).setCursor(wlPointerResource,
                                             serial,
                                             wlSurfaceResource,
                                             hotspotX,
                                             hotspotY);
    }

    @Test
    public void testSetCursorOtherRoleSurface() {
        //given: a pointer with a surface with a role that is not this pointer device
        final WlPointerResource wlPointerResource = mock(WlPointerResource.class);
        final Client            client            = mock(Client.class);
        when(wlPointerResource.getClient()).thenReturn(client);
        final Resource displayResource = mock(Resource.class);
        when(client.getObject(Display.OBJECT_ID)).thenReturn(displayResource);
        final int               serial            = 12345;
        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
        final WlSurface         wlSurface         = mock(WlSurface.class);
        when(wlSurfaceResource.getImplementation()).thenReturn(wlSurface);
        final Surface surface = mock(Surface.class);
        when(wlSurface.getSurface()).thenReturn(surface);
        when(surface.getRole()).thenReturn(Optional.of(new Role() {}));

        final int hotspotX = 10;
        final int hotspotY = 15;

        //when: SetCursor is called
        this.wlPointer.setCursor(wlPointerResource,
                                 serial,
                                 wlSurfaceResource,
                                 hotspotX,
                                 hotspotY);

        //then: a protocol error is raised.
        verify(displayResource).postError(eq(WlPointerError.ROLE.value),
                                          anyString());
    }
}