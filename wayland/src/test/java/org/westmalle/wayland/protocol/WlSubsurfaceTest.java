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
import org.freedesktop.wayland.server.WlCompositorResource;
import org.freedesktop.wayland.server.WlSubsurfaceResource;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.freedesktop.wayland.server.jna.WaylandServerLibrary;
import org.freedesktop.wayland.server.jna.WaylandServerLibraryMapping;
import org.freedesktop.wayland.shared.WlSubsurfaceError;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.westmalle.wayland.core.Compositor;
import org.westmalle.wayland.core.Point;
import org.westmalle.wayland.core.Subsurface;
import org.westmalle.wayland.core.Surface;

import java.util.LinkedList;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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
        //given: a wlsubsurface, a subsurface, an x and y coordinate
        final WlSubsurfaceResource wlSubsurfaceResource = mock(WlSubsurfaceResource.class);
        final int                  x                    = 123;
        final int                  y                    = 456;

        //when: set position is called
        this.wlSubsurface.setPosition(wlSubsurfaceResource,
                                      x,
                                      y);

        //then: subsurface set position is called with x and y coordinates
        verify(this.subsurface).setPosition(eq(Point.create(x,
                                                            y)));
    }

    private void setupSibling(final WlSubsurfaceResource wlSubsurfaceResource,
                              final WlSurfaceResource siblingWlSurfaceResource,
                              final boolean siblingValid) {
        final WlSubsurface      wlSubsurface            = mock(WlSubsurface.class);
        final WlSurfaceResource parentWlSurfaceResource = mock(WlSurfaceResource.class);

        when(wlSubsurfaceResource.getImplementation()).thenReturn(wlSubsurface);
        when(wlSubsurface.getSubsurface()).thenReturn(this.subsurface);
        when(this.subsurface.getParentWlSurfaceResource()).thenReturn(parentWlSurfaceResource);

        final WlSurface            siblingWlSurface     = mock(WlSurface.class);
        final Surface              siblingSurface       = mock(Surface.class);
        final WlCompositorResource wlCompositorResource = mock(WlCompositorResource.class);
        final WlCompositor         wlCompositor         = mock(WlCompositor.class);
        final Compositor           compositor           = mock(Compositor.class);

        when(siblingWlSurfaceResource.getImplementation()).thenReturn(siblingWlSurface);
        when(siblingWlSurface.getSurface()).thenReturn(siblingSurface);
        when(siblingSurface.getWlCompositorResource()).thenReturn(wlCompositorResource);
        when(wlCompositorResource.getImplementation()).thenReturn(wlCompositor);
        when(wlCompositor.getCompositor()).thenReturn(compositor);

        final LinkedList<WlSurfaceResource> subsurfaceStack = mock(LinkedList.class);

        when(compositor.getSubsurfaceStack(parentWlSurfaceResource)).thenReturn(subsurfaceStack);
        when(subsurfaceStack.contains(siblingWlSurfaceResource)).thenReturn(siblingValid);
    }

    @Test
    public void testPlaceAbove() throws Exception {
        //given: a wlsubsurface, a subsurface, a sibling or parent
        final WlSubsurfaceResource wlSubsurfaceResource     = mock(WlSubsurfaceResource.class);
        final WlSurfaceResource    siblingWlSurfaceResource = mock(WlSurfaceResource.class);

        setupSibling(wlSubsurfaceResource,
                     siblingWlSurfaceResource,
                     true);

        //when: place above is called
        this.wlSubsurface.placeAbove(wlSubsurfaceResource,
                                     siblingWlSurfaceResource);

        //then: above is called on the subsurface
        verify(this.subsurface).above(siblingWlSurfaceResource);
    }

    @Test
    public void testPlaceAboveBadSibling() throws Exception {
        //given: a wlsubsurface, a bad sibling or parent
        final WlSubsurfaceResource wlSubsurfaceResource     = mock(WlSubsurfaceResource.class);
        final WlSurfaceResource    siblingWlSurfaceResource = mock(WlSurfaceResource.class);

        setupSibling(wlSubsurfaceResource,
                     siblingWlSurfaceResource,
                     false);

        //when: place above is called
        this.wlSubsurface.placeAbove(wlSubsurfaceResource,
                                     siblingWlSurfaceResource);

        //then: a protocol error is raised
        verify(wlSubsurfaceResource).postError(eq(WlSubsurfaceError.BAD_SURFACE.getValue()),
                                               anyString());
    }

    @Test
    public void testPlaceBelow() throws Exception {
        //given: a wlsubsurface, a subsurface, a sibling or parent
        final WlSubsurfaceResource wlSubsurfaceResource     = mock(WlSubsurfaceResource.class);
        final WlSurfaceResource    siblingWlSurfaceResource = mock(WlSurfaceResource.class);

        setupSibling(wlSubsurfaceResource,
                     siblingWlSurfaceResource,
                     true);

        //when: place below is called
        this.wlSubsurface.placeBelow(wlSubsurfaceResource,
                                     siblingWlSurfaceResource);

        //then: below is called on the subsurface
        verify(this.subsurface).below(siblingWlSurfaceResource);
    }

    @Test
    public void testPlaceBelowBadSibling() throws Exception {
        //given: a wlsubsurface, a bad sibling or parent
        final WlSubsurfaceResource wlSubsurfaceResource     = mock(WlSubsurfaceResource.class);
        final WlSurfaceResource    siblingWlSurfaceResource = mock(WlSurfaceResource.class);

        setupSibling(wlSubsurfaceResource,
                     siblingWlSurfaceResource,
                     false);

        //when: place below is called
        this.wlSubsurface.placeBelow(wlSubsurfaceResource,
                                     siblingWlSurfaceResource);

        //then: a protocol error is raised
        verify(wlSubsurfaceResource).postError(eq(WlSubsurfaceError.BAD_SURFACE.getValue()),
                                               anyString());
    }

    @Test
    public void testSetSync() throws Exception {
        //given: a wlsubsurface, a subsurface
        final WlSubsurfaceResource wlSubsurfaceResource = mock(WlSubsurfaceResource.class);

        //when: set sync is called
        this.wlSubsurface.setSync(wlSubsurfaceResource);

        //then: sync is called on the subsurface
        verify(this.subsurface).setSync(true);
    }

    @Test
    public void testSetDesync() throws Exception {
        //given: a wlsubsurface, a subsurface
        final WlSubsurfaceResource wlSubsurfaceResource = mock(WlSubsurfaceResource.class);

        //when: set desync is called
        this.wlSubsurface.setDesync(wlSubsurfaceResource);

        //then: desync is called on the subsurface
        verify(this.subsurface).setSync(false);
    }
}