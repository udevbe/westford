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
import org.freedesktop.wayland.server.WlSubsurfaceResource;
import org.freedesktop.wayland.server.jna.WaylandServerLibrary;
import org.freedesktop.wayland.server.jna.WaylandServerLibraryMapping;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.westmalle.wayland.core.Subsurface;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(WaylandServerLibrary.class)
public class WlSubsurfaceTest {

    @Mock
    private Subsurface subsurface;

    @Mock
    private WaylandServerLibraryMapping waylandServerLibraryMapping;

    private WlSubsurface wlSubsurface;

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(WaylandServerLibrary.class);
        when(WaylandServerLibrary.INSTANCE()).thenReturn(this.waylandServerLibraryMapping);
        this.wlSubsurface = new WlSubsurface(this.subsurface);
    }

    @Test
    public void testCreate() throws Exception {
        //given
        final Client client  = mock(Client.class);
        final int    version = 1;
        final int    id      = 15;
        //when
        final WlSubsurfaceResource wlSubsurfaceResource = this.wlSubsurface.create(client,
                                                                                   version,
                                                                                   id);
        //then
        assertThat(wlSubsurfaceResource).isNotNull();
        assertThat(wlSubsurfaceResource.getImplementation()).isSameAs(this.wlSubsurface);
    }

    @Test
    public void testSetPosition() throws Exception {
        //TODO
        //given: a wlsubsurface, a subsurface, an x and y coordinate
        //when: set position is called
        //then: subsurface set position is called with x and y coordinates
    }

    @Test
    public void testPlaceAbove() throws Exception {
        //TODO
        //given: a wlsubsurface, a subsurface, a sibling or parent
        //when: place above is called
        //then: above is called on the subsurface
    }

    @Test
    public void testPlaceAboveBadSibling() throws Exception {
        //TODO
        //given: a wlsubsurface, a bad sibling or parent
        //when: place above is called
        //then: a protocol error is raised
    }

    @Test
    public void testPlaceBelow() throws Exception {
        //TODO
        //given: a wlsubsurface, a subsurface, a sibling or parent
        //when: place below is called
        //then: below is called on the subsurface
    }

    @Test
    public void testPlaceBelowBadSibling() throws Exception {
        //TODO
        //given: a wlsubsurface, a bad sibling or parent
        //when: place below is called
        //then: a protocol error is raised
    }

    @Test
    public void testSetSync() throws Exception {
        //TODO
        //given: a wlsubsurface, a subsurface
        //when: set sync is called
        //then: sync is called on the subsurface
    }

    @Test
    public void testSetDesync() throws Exception {
        //TODO
        //given: a wlsubsurface, a subsurface
        //when: set desync is called
        //then: desync is called on the subsurface
    }
}