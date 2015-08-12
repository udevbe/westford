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
import org.freedesktop.wayland.server.WlPointerResource;
import org.freedesktop.wayland.server.jna.WaylandServerLibrary;
import org.freedesktop.wayland.server.jna.WaylandServerLibraryMapping;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.westmalle.wayland.core.PointerDevice;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(WaylandServerLibrary.class)
public class WlPointerTest {

    @Mock
    private PointerDevice               pointerDevice;
    @Mock
    private WaylandServerLibraryMapping waylandServerLibraryMapping;

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(WaylandServerLibrary.class);
        when(WaylandServerLibrary.INSTANCE()).thenReturn(this.waylandServerLibraryMapping);
    }

    @Test
    public void testRelease() throws Exception {
        //given
        final WlPointerResource wlPointerResource = mock(WlPointerResource.class);
        final WlPointer         wlPointer         = new WlPointer(this.pointerDevice);
        //when
        wlPointer.release(wlPointerResource);
        //then
        verify(wlPointerResource).destroy();
    }

    @Test
    public void testCreate() throws Exception {
        //given
        final Client    client    = mock(Client.class);
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
        //when: SetCursor is called
        //then: cursor is removed on pointer device
        throw new UnsupportedOperationException();
    }

    @Test
    public void testSetCursor() {
        //given: a pointer with a surface with no role
        //when: SetCursor is called
        //then: cursor is set on pointer device and role is set for surface
        throw new UnsupportedOperationException();
    }

    @Test
    public void testSetCursorSameSurface() {
        //given: a pointer with a surface with a role that is this pointer device
        //when: SetCursor is called
        //then: cursor is set on pointer device
        throw new UnsupportedOperationException();
    }

    @Test
    public void testSetCursorOtherRoleSurface() {
        //given: a pointer with a surface with a role that is not this pointer device
        //when: SetCursor is called
        //then: a protocol error is raised.
        throw new UnsupportedOperationException();
    }
}