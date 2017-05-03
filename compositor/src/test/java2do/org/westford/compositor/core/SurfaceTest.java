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
package org.westford.compositor.core;

import org.freedesktop.wayland.server.DestroyListener;
import org.freedesktop.wayland.server.WlBufferResource;
import org.freedesktop.wayland.server.WlCallbackResource;
import org.freedesktop.wayland.server.WlRegionResource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.westford.compositor.protocol.WlRegion;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({FiniteRegionFactory.class})
public class SurfaceTest {

    @Mock
    private FiniteRegionFactory finiteRegionFactory;
    @Mock
    private Compositor          compositor;
    @Mock
    private Renderer            renderer;
    @Mock
    private SurfaceViewFactory  surfaceViewFactory;

    @InjectMocks
    private Surface surface;

    @Test
    public void testMarkDestroyed() throws Exception {
        //given
        //when
        this.surface.markDestroyed();
        //then
        assertThat(this.surface.isDestroyed()).isTrue();
    }

    @Test
    public void testMarkDamaged() throws Exception {
        //given
        final FiniteRegion region = mock(FiniteRegion.class);
        when(this.finiteRegionFactory.create()).thenReturn(region);
        final Rectangle damage = Rectangle.Companion.create(100,
                                                            100,
                                                            20,
                                                            50);
        //when
        this.surface.markDamaged(damage);
        //then
        verify(region).add(damage);
    }

    @Test
    public void testAttachCommit() throws Exception {
        //given
        final WlBufferResource wlBufferResource = mock(WlBufferResource.class);
        final Integer          relX             = -10;
        final Integer          relY             = 200;

        final Buffer buffer      = mock(Buffer.class);
        final int    bufferWidth = 200;
        when(buffer.getWidth()).thenReturn(bufferWidth);
        final int bufferHeight = 300;
        when(buffer.getHeight()).thenReturn(bufferHeight);
        when(this.renderer.queryBuffer(wlBufferResource)).thenReturn(buffer);

        //when
        this.surface.attachBuffer(wlBufferResource,
                                  relX,
                                  relY);
        this.surface.commit();
        //then
        assertThat(this.surface.getState()
                               .getBuffer()
                               .isPresent()).isTrue();
        assertThat(this.surface.getSize()).isEqualTo(Rectangle.Companion.builder()
                                                                        .width(200)
                                                                        .height(300)
                                                                        .build());
        verify(this.compositor).requestRender();
    }

    @Test
    public void testAttachCommitAttachCommit() throws Exception {
        //given
        final WlBufferResource wlBufferResource = mock(WlBufferResource.class);
        final Integer          relX             = -10;
        final Integer          relY             = 200;

        final Buffer buffer      = mock(Buffer.class);
        final int    bufferWidth = 200;
        when(buffer.getWidth()).thenReturn(bufferWidth);
        final int bufferHeight = 300;
        when(buffer.getHeight()).thenReturn(bufferHeight);
        when(this.renderer.queryBuffer(wlBufferResource)).thenReturn(buffer);

        //when
        this.surface.attachBuffer(wlBufferResource,
                                  relX,
                                  relY);
        this.surface.commit();
        this.surface.attachBuffer(wlBufferResource,
                                  relX,
                                  relY);
        this.surface.commit();
        //then
        assertThat(this.surface.getSize()).isEqualTo(Rectangle.Companion.builder()
                                                                        .width(200)
                                                                        .height(300)
                                                                        .build());
        verify(wlBufferResource).release();
    }

    @Test
    public void testAttachAttachCommit() throws Exception {
        //given
        final WlBufferResource wlBufferResource0 = mock(WlBufferResource.class);
        final Integer          relX0             = -10;
        final Integer          relY0             = 200;

        final WlBufferResource wlBufferResource1 = mock(WlBufferResource.class);
        final Integer          relX1             = -10;
        final Integer          relY1             = 200;

        final Buffer buffer0      = mock(Buffer.class);
        final int    bufferWidth0 = 200;
        when(buffer0.getWidth()).thenReturn(bufferWidth0);
        final int bufferHeight0 = 300;
        when(buffer0.getHeight()).thenReturn(bufferHeight0);
        when(this.renderer.queryBuffer(wlBufferResource0)).thenReturn(buffer0);

        final Buffer buffer1      = mock(Buffer.class);
        final int    bufferWidth1 = 123;
        when(buffer1.getWidth()).thenReturn(bufferWidth1);
        final int bufferHeight1 = 456;
        when(buffer1.getHeight()).thenReturn(bufferHeight1);
        when(this.renderer.queryBuffer(wlBufferResource1)).thenReturn(buffer1);

        //when
        this.surface.attachBuffer(wlBufferResource0,
                                  relX0,
                                  relY0);
        this.surface.attachBuffer(wlBufferResource1,
                                  relX1,
                                  relY1);
        this.surface.commit();
        //then
        assertThat(this.surface.getState()
                               .getBuffer()
                               .get()).isSameAs(wlBufferResource1);
        assertThat(this.surface.getSize()).isEqualTo(Rectangle.Companion.builder()
                                                                        .width(123)
                                                                        .height(456)
                                                                        .build());
    }

    @Test
    public void testAttachDestroyBuffer() {
        //given
        final WlBufferResource wlBufferResource = mock(WlBufferResource.class);
        final Integer          relX0            = -10;
        final Integer          relY0            = 200;

        //when
        this.surface.attachBuffer(wlBufferResource,
                                  relX0,
                                  relY0);
        //then
        final ArgumentCaptor<DestroyListener> listenerArgumentCaptor = ArgumentCaptor.forClass(DestroyListener.class);
        verify(wlBufferResource,
               times(1)).register(listenerArgumentCaptor.capture());
        final DestroyListener destroyListener = listenerArgumentCaptor.getValue();
        //and when
        destroyListener.handle();
        //then
        this.surface.detachBuffer();
    }

    @Test
    public void testAttachDetachCommit() throws Exception {
        //given
        final WlBufferResource wlBufferResource = mock(WlBufferResource.class);
        final Integer          relX             = -10;
        final Integer          relY             = 200;

        final Buffer buffer      = mock(Buffer.class);
        final int    bufferWidth = 200;
        when(buffer.getWidth()).thenReturn(bufferWidth);
        final int bufferHeight = 300;
        when(buffer.getHeight()).thenReturn(bufferHeight);
        when(this.renderer.queryBuffer(wlBufferResource)).thenReturn(buffer);

        //when
        this.surface.attachBuffer(wlBufferResource,
                                  relX,
                                  relY);
        this.surface.detachBuffer();
        this.surface.commit();
        //then
        assertThat(this.surface.getState()
                               .getBuffer()
                               .isPresent()).isFalse();
        assertThat(this.surface.getSize()).isEqualTo(Rectangle.Companion.getZERO());
        verify(this.compositor).requestRender();
    }

    @Test
    public void testRemoveOpaqueRegion() throws Exception {
        //given
        final WlRegionResource wlRegionResource = mock(WlRegionResource.class);
        final WlRegion         wlRegion         = mock(WlRegion.class);
        when(wlRegionResource.getImplementation()).thenReturn(wlRegion);
        final Region region = mock(Region.class);
        when(wlRegion.getRegion()).thenReturn(region);
        this.surface.setOpaqueRegion(wlRegionResource);
        //when
        this.surface.removeOpaqueRegion();
        //then
        assertThat(this.surface.getState()
                               .getOpaqueRegion()
                               .isPresent()).isFalse();
    }

    @Test
    public void testRemoveInputRegion() throws Exception {
        //given
        final WlRegionResource wlRegionResource = mock(WlRegionResource.class);
        final WlRegion         wlRegion         = mock(WlRegion.class);
        when(wlRegionResource.getImplementation()).thenReturn(wlRegion);
        final Region region = mock(Region.class);
        when(wlRegion.getRegion()).thenReturn(region);

        this.surface.setInputRegion(wlRegionResource);
        //when
        this.surface.removeInputRegion();
        //then
        assertThat(this.surface.getState()
                               .getInputRegion()
                               .isPresent()).isFalse();
    }

    @Test
    public void testFirePaintCallbacks() throws Exception {
        //given
        final int serial = 548674;

        final WlCallbackResource wlCallbackResource0 = mock(WlCallbackResource.class);
        final WlCallbackResource wlCallbackResource1 = mock(WlCallbackResource.class);
        final WlCallbackResource wlCallbackResource2 = mock(WlCallbackResource.class);

        this.surface.addCallback(wlCallbackResource0);
        this.surface.addCallback(wlCallbackResource1);
        this.surface.addCallback(wlCallbackResource2);
        //when
        this.surface.firePaintCallbacks(serial);
        //then
        verify(wlCallbackResource0).done(serial);
        verify(wlCallbackResource1).done(serial);
        verify(wlCallbackResource2).done(serial);
        assertThat(this.surface.getFrameCallbacks()).isEmpty();
    }

    //TODO move to surfaceview test
//    @Test
//    public void testLocal() throws Exception {
//        //given
//        final Point absoluteCoordinate = Point.create(150,
//                                                      150);
//        final Point surfaceCoordinate = Point.create(100,
//                                                     100);
//        this.surface.setPosition(surfaceCoordinate);
//        //when
//        final Point relativeCoordinate = this.surface.local(absoluteCoordinate);
//        //then
//        assertThat(relativeCoordinate).isEqualTo(Point.create(50,
//                                                              50));
//    }

    @Test
    public void testUpdateSizeNoScaling() throws Exception {
        //given
        final WlBufferResource wlBufferResource = mock(WlBufferResource.class);

        final Buffer buffer      = mock(Buffer.class);
        final int    bufferWidth = 100;
        when(buffer.getWidth()).thenReturn(bufferWidth);
        final int bufferHeight = 100;
        when(buffer.getHeight()).thenReturn(bufferHeight);
        when(this.renderer.queryBuffer(wlBufferResource)).thenReturn(buffer);

        this.surface.attachBuffer(wlBufferResource,
                                  0,
                                  0);
        //when
        this.surface.commit();

        //then
        assertThat(this.surface.getSize()).isEqualTo(Rectangle.Companion.create(0,
                                                                                0,
                                                                                100,
                                                                                100));
    }

    @Test
    public void testUpdateSizeBufferAbsent() throws Exception {
        //given
        //when
        this.surface.commit();

        //then
        assertThat(this.surface.getSize()).isEqualTo(Rectangle.Companion.getZERO());
    }

    @Test
    public void testUpdateSizeScaling() throws Exception {
        //given
        final WlBufferResource wlBufferResource = mock(WlBufferResource.class);

        final Buffer buffer      = mock(Buffer.class);
        final int    bufferWidth = 100;
        when(buffer.getWidth()).thenReturn(bufferWidth);
        final int bufferHeight = 100;
        when(buffer.getHeight()).thenReturn(bufferHeight);
        when(this.renderer.queryBuffer(wlBufferResource)).thenReturn(buffer);

        this.surface.setScale(5);

        this.surface.attachBuffer(wlBufferResource,
                                  0,
                                  0);
        //when
        this.surface.commit();

        //then
        assertThat(this.surface.getSize()).isEqualTo(Rectangle.Companion.create(0,
                                                                                0,
                                                                                20,
                                                                                20));
    }

    //TODO sibling tests (move, add, remove, pending subsurface, ...)
}