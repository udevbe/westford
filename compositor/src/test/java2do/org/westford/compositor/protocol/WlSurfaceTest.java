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
import org.freedesktop.wayland.server.WlBufferResource;
import org.freedesktop.wayland.server.WlCallbackResource;
import org.freedesktop.wayland.server.WlCompositorResource;
import org.freedesktop.wayland.server.WlRegionResource;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.freedesktop.wayland.server.jaccall.WaylandServerCore;
import org.freedesktop.wayland.shared.WlOutputTransform;
import org.freedesktop.wayland.util.ObjectCache;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.westford.compositor.core.Rectangle;
import org.westford.compositor.core.Role;
import org.westford.compositor.core.Surface;
import org.westford.compositor.core.calc.Mat4;

import java.util.Optional;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
                        //following classes have static methods, so we have to powermock them:
                        WaylandServerCore.class,
                        //following classes are final, so we have to powermock them:
                        WlCallbackFactory.class
                })
public class WlSurfaceTest {

    @Mock
    private WlCallbackFactory    wlCallbackFactory;
    @Mock
    private WlCompositorResource compositorResource;
    @Mock
    private Surface              surface;

    @Mock
    private WaylandServerCore waylandServerCore;

    private WlSurface wlSurface;

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(WaylandServerCore.class);
        when(WaylandServerCore.INSTANCE()).thenReturn(this.waylandServerCore);
        ObjectCache.remove(112358L);
        when(this.waylandServerCore.wl_resource_create(anyLong(),
                                                       anyLong(),
                                                       anyInt(),
                                                       anyInt())).thenReturn(112358L);
        this.wlSurface = new WlSurface(this.wlCallbackFactory,
                                       this.surface);
    }

    @Test
    public void testSetBufferTransform() throws Exception {
        //given
        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
        final int               transform         = WlOutputTransform.NORMAL.value;

        //when
        this.wlSurface.setBufferTransform(wlSurfaceResource,
                                          transform);
        //then
        verify(this.surface).setBufferTransform(Mat4.Companion.getIDENTITY());

    }

    @Test
    public void testCreate() throws Exception {
        //given
        final Client client = mock(Client.class);
        Whitebox.setInternalState(client,
                                  "pointer",
                                  2468L);
        final int version = 5;
        final int id      = 100;

        //when
        final WlSurfaceResource wlSurfaceResource = this.wlSurface.create(client,
                                                                          version,
                                                                          id);
        //then
        assertThat(wlSurfaceResource).isNotNull();
        assertThat(wlSurfaceResource.getImplementation()).isSameAs(this.wlSurface);
    }

    @Test
    public void testDestroy() throws Exception {
        //given
        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);

        //when
        this.wlSurface.destroy(wlSurfaceResource);
        //then
        verify(wlSurfaceResource,
               times(1)).destroy();
    }

    @Test
    public void testAttach() throws Exception {
        //given
        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
        final WlBufferResource  wlBufferResource  = mock(WlBufferResource.class);
        final int               x                 = 11;
        final int               y                 = 22;

        //when
        this.wlSurface.attach(wlSurfaceResource,
                              wlBufferResource,
                              x,
                              y);
        //then
        verify(this.surface).attachBuffer(wlBufferResource,
                                          x,
                                          y);
    }

    @Test
    public void testDoubleAttach() throws Exception {
        //given
        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
        final WlBufferResource  wlBufferResource0 = mock(WlBufferResource.class);
        final WlBufferResource  wlBufferResource1 = mock(WlBufferResource.class);
        final int               x                 = 11;
        final int               y                 = 22;

        //when
        this.wlSurface.attach(wlSurfaceResource,
                              wlBufferResource0,
                              x,
                              y);
        this.wlSurface.attach(wlSurfaceResource,
                              wlBufferResource1,
                              x,
                              y);
        //then
        verify(this.surface).attachBuffer(wlBufferResource0,
                                          x,
                                          y);
        verify(this.surface).attachBuffer(wlBufferResource1,
                                          x,
                                          y);
    }

    @Test
    public void testDetach() throws Exception {
        //given
        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);

        //when
        this.wlSurface.attach(wlSurfaceResource,
                              null,
                              0,
                              0);
        //then
        verify(this.surface).detachBuffer();
    }

    @Test
    public void testAttachDetach() throws Exception {
        //given
        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
        final WlBufferResource  wlBufferResource  = mock(WlBufferResource.class);
        final int               x                 = 11;
        final int               y                 = 22;

        //when
        this.wlSurface.attach(wlSurfaceResource,
                              wlBufferResource,
                              x,
                              y);
        this.wlSurface.attach(wlSurfaceResource,
                              null,
                              0,
                              0);
        //then
        verify(this.surface,
               times(1)).attachBuffer(wlBufferResource,
                                      x,
                                      y);
        verify(this.surface,
               times(1)).detachBuffer();
    }

    @Test
    public void testDamage() throws Exception {
        //given
        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
        final int               x                 = -20;
        final int               y                 = -100;
        final int               width             = 500;
        final int               height            = 1000;

        //when
        this.wlSurface.damage(wlSurfaceResource,
                              x,
                              y,
                              width,
                              height);
        //then
        verify(this.surface,
               times(1)).markDamaged(eq(Rectangle.Companion.create(x,
                                                                   y,
                                                                   width,
                                                                   height)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDamageNegativeWidthNegativeHeight() throws Exception {
        //given
        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
        final int               x                 = 20;
        final int               y                 = 100;
        final int               width             = -500;
        final int               height            = -1000;

        //when
        this.wlSurface.damage(wlSurfaceResource,
                              x,
                              y,
                              width,
                              height);
        //then
    }

    @Test
    public void testFrame() throws Exception {
        //given
        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
        final int               callbackId        = 987;

        final Client client  = mock(Client.class);
        final int    version = 5;

        when(wlSurfaceResource.getClient()).thenReturn(client);
        when(wlSurfaceResource.getVersion()).thenReturn(version);

        final WlCallback wlCallback = mock(WlCallback.class);
        when(this.wlCallbackFactory.create()).thenReturn(wlCallback);

        final WlCallbackResource wlCallbackResource = mock(WlCallbackResource.class);
        when(wlCallback.add(client,
                            version,
                            callbackId)).thenReturn(wlCallbackResource);
        //when
        this.wlSurface.frame(wlSurfaceResource,
                             callbackId);
        //then
        verify(this.surface).addCallback(wlCallbackResource);
    }

    @Test
    public void testSetOpaqueRegion() throws Exception {
        //given
        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
        final WlRegionResource  wlRegionResource  = mock(WlRegionResource.class);

        //when
        this.wlSurface.setOpaqueRegion(wlSurfaceResource,
                                       wlRegionResource);
        //then
        verify(this.surface).setOpaqueRegion(wlRegionResource);
    }

    @Test
    public void testRemoveOpaqueRegion() throws Exception {
        //given
        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);

        //when
        this.wlSurface.setOpaqueRegion(wlSurfaceResource,
                                       null);
        //then
        verify(this.surface).removeOpaqueRegion();
    }

    @Test
    public void testSetInputRegion() throws Exception {
        //given
        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
        final WlRegionResource  wlRegionResource  = mock(WlRegionResource.class);

        //when
        this.wlSurface.setInputRegion(wlSurfaceResource,
                                      wlRegionResource);
        //then
        verify(this.surface).setInputRegion(wlRegionResource);
    }

    @Test
    public void testRemoveInputRegion() throws Exception {
        //given
        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);

        //when
        this.wlSurface.setInputRegion(wlSurfaceResource,
                                      null);
        //then
        verify(this.surface).removeInputRegion();
    }

    @Test
    public void testCommit() throws Exception {
        //given
        final Role              role              = mock(Role.class);
        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
        final WlBufferResource  wlBufferResource  = mock(WlBufferResource.class);
        final int               x                 = 11;
        final int               y                 = 22;

        when(this.surface.getRole()).thenReturn(Optional.of(role));

        this.wlSurface.attach(wlSurfaceResource,
                              wlBufferResource,
                              x,
                              y);
        //when
        this.wlSurface.commit(wlSurfaceResource);
        //then
        verify(role).beforeCommit(wlSurfaceResource);
        verify(this.surface).commit();
    }

    @Test
    public void testCommitNoBuffer() throws Exception {
        //given
        final Role              role              = mock(Role.class);
        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
        when(this.surface.getRole()).thenReturn(Optional.of(role));
        //when
        this.wlSurface.commit(wlSurfaceResource);
        //then
        verify(role).beforeCommit(wlSurfaceResource);
        verify(this.surface).commit();
    }
}