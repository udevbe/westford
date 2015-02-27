package org.westmalle.wayland.output;

import org.freedesktop.wayland.server.ShmBuffer;
import org.freedesktop.wayland.server.WlBufferResource;
import org.freedesktop.wayland.server.WlCallbackResource;
import org.freedesktop.wayland.server.WlCompositorResource;
import org.freedesktop.wayland.server.WlRegionResource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.westmalle.wayland.protocol.WlCompositor;

import javax.media.nativewindow.util.Point;
import javax.media.nativewindow.util.PointImmutable;
import javax.media.nativewindow.util.Rectangle;
import javax.media.nativewindow.util.RectangleImmutable;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({RegionFactory.class,
                 ShmBuffer.class})
public class SurfaceTest {

    @Mock
    private RegionFactory        regionFactory;
    @Mock
    private WlCompositorResource wlCompositorResource;
    @InjectMocks
    private Surface              surface;

    @Before
    public void setUp(){
        mockStatic(ShmBuffer.class);
    }

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
        final Region region = mock(Region.class);
        when(region.add(any())).thenReturn(region);
        when(this.regionFactory.create()).thenReturn(region);
        final RectangleImmutable damage = new Rectangle(100,
                                                        100,
                                                        20,
                                                        50);
        //when
        this.surface.markDamaged(damage);
        //then
        verify(region,
               times(1)).add(damage);
    }

    @Test
    public void testAttachCommit() throws Exception {
        //given
        final WlCompositor wlCompositor = mock(WlCompositor.class);
        when(this.wlCompositorResource.getImplementation()).thenReturn(wlCompositor);

        final Compositor compositor = mock(Compositor.class);
        when(wlCompositor.getCompositor()).thenReturn(compositor);

        final WlBufferResource buffer = mock(WlBufferResource.class);
        final Integer relX = -10;
        final Integer relY = 200;

        final ShmBuffer shmBuffer = mock(ShmBuffer.class);
        when(ShmBuffer.get(buffer)).thenReturn(shmBuffer);

        final int bufferWidth = 200;
        when(shmBuffer.getWidth()).thenReturn(bufferWidth);
        final int bufferHeight = 300;
        when(shmBuffer.getHeight()).thenReturn(bufferHeight);

        //when
        this.surface.attachBuffer(buffer,
                                  relX,
                                  relY);
        this.surface.commit();
        //then
        assertThat(this.surface.getState()
                               .getBuffer()
                               .isPresent()).isTrue();
        assertThat(this.surface.getSize()).isEqualTo(new Rectangle(0,0,200,300));
        verify(compositor,
               times(1)).requestRender();
    }

    @Test
    public void testAttachCommitAttachCommit() throws Exception {
        //given
        final WlCompositor wlCompositor = mock(WlCompositor.class);
        when(this.wlCompositorResource.getImplementation()).thenReturn(wlCompositor);

        final Compositor compositor = mock(Compositor.class);
        when(wlCompositor.getCompositor()).thenReturn(compositor);

        final WlBufferResource buffer = mock(WlBufferResource.class);
        final Integer relX = -10;
        final Integer relY = 200;

        final ShmBuffer shmBuffer = mock(ShmBuffer.class);
        when(ShmBuffer.get(buffer)).thenReturn(shmBuffer);

        final int bufferWidth = 200;
        when(shmBuffer.getWidth()).thenReturn(bufferWidth);
        final int bufferHeight = 300;
        when(shmBuffer.getHeight()).thenReturn(bufferHeight);

        //when
        this.surface.attachBuffer(buffer,
                                  relX,
                                  relY);
        this.surface.commit();
        this.surface.attachBuffer(buffer,
                                  relX,
                                  relY);
        this.surface.commit();
        //then
        assertThat(this.surface.getSize()).isEqualTo(new Rectangle(0,0,200,300));
        verify(buffer,
               times(1)).release();
    }

    @Test
    public void testAttachAttachCommit() throws Exception {
        //given
        final WlCompositor wlCompositor = mock(WlCompositor.class);
        when(this.wlCompositorResource.getImplementation()).thenReturn(wlCompositor);

        final Compositor compositor = mock(Compositor.class);
        when(wlCompositor.getCompositor()).thenReturn(compositor);

        final WlBufferResource buffer0 = mock(WlBufferResource.class);
        final Integer relX0 = -10;
        final Integer relY0 = 200;

        final WlBufferResource buffer1 = mock(WlBufferResource.class);
        final Integer relX1 = -10;
        final Integer relY1 = 200;

        final ShmBuffer shmBuffer0 = mock(ShmBuffer.class);
        when(ShmBuffer.get(buffer0)).thenReturn(shmBuffer0);

        final int bufferWidth0 = 200;
        when(shmBuffer0.getWidth()).thenReturn(bufferWidth0);
        final int bufferHeight0 = 300;
        when(shmBuffer0.getHeight()).thenReturn(bufferHeight0);

        final ShmBuffer shmBuffer1 = mock(ShmBuffer.class);
        when(ShmBuffer.get(buffer1)).thenReturn(shmBuffer1);

        final int bufferWidth1 = 123;
        when(shmBuffer1.getWidth()).thenReturn(bufferWidth1);
        final int bufferHeight1 = 456;
        when(shmBuffer1.getHeight()).thenReturn(bufferHeight1);

        //when
        this.surface.attachBuffer(buffer0,
                                  relX0,
                                  relY0);
        this.surface.attachBuffer(buffer1,
                                  relX1,
                                  relY1);
        this.surface.commit();
        //then
        assertThat(this.surface.getState()
                               .getBuffer()
                               .get()).isSameAs(buffer1);
        assertThat(this.surface.getSize()).isEqualTo(new Rectangle(0,0,123,456));

    }

    @Test
    public void testAttachDetachCommit() throws Exception {
        //given
        final WlCompositor wlCompositor = mock(WlCompositor.class);
        when(this.wlCompositorResource.getImplementation()).thenReturn(wlCompositor);

        final Compositor compositor = mock(Compositor.class);
        when(wlCompositor.getCompositor()).thenReturn(compositor);

        final WlBufferResource buffer = mock(WlBufferResource.class);
        final Integer relX = -10;
        final Integer relY = 200;

        final ShmBuffer shmBuffer = mock(ShmBuffer.class);
        when(ShmBuffer.get(buffer)).thenReturn(shmBuffer);

        //when
        this.surface.attachBuffer(buffer,
                                  relX,
                                  relY);
        this.surface.detachBuffer();
        this.surface.commit();
        //then
        assertThat(this.surface.getState()
                               .getBuffer()
                               .isPresent()).isFalse();
        assertThat(this.surface.getSize()).isEqualTo(new Rectangle(0,0,0,0));
        verify(compositor,
               times(1)).requestRender();
    }

    @Test
    public void testRemoveOpaqueRegion() throws Exception {
        //given
        final WlRegionResource wlRegionResource = mock(WlRegionResource.class);
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
        verify(wlCallbackResource0,
               times(1)).done(serial);
        verify(wlCallbackResource1,
               times(1)).done(serial);
        verify(wlCallbackResource2,
               times(1)).done(serial);
        assertThat(this.surface.getFrameCallbacks()).isEmpty();
    }

    @Test
    public void testLocal() throws Exception {
        //given
        final WlCompositor wlCompositor = mock(WlCompositor.class);
        when(this.wlCompositorResource.getImplementation()).thenReturn(wlCompositor);

        final Compositor compositor = mock(Compositor.class);
        when(wlCompositor.getCompositor()).thenReturn(compositor);

        final PointImmutable absoluteCoordinate = new Point(150,
                                                            150);
        final PointImmutable surfaceCoordinate = new Point(100,
                                                           100);
        this.surface.setPosition(surfaceCoordinate);
        //when
        final PointImmutable relativeCoordinate = this.surface.local(absoluteCoordinate);
        //then
        assertThat(relativeCoordinate).isEqualTo(new Point(50,
                                                           50));
    }
}