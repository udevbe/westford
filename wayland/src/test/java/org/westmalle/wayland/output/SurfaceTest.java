package org.westmalle.wayland.output;

import com.hackoeur.jglm.Mat;
import com.hackoeur.jglm.Mat3;
import com.hackoeur.jglm.Mat4;

import org.freedesktop.wayland.server.WlBufferResource;
import org.freedesktop.wayland.server.WlCallbackResource;
import org.freedesktop.wayland.server.WlCompositorResource;
import org.freedesktop.wayland.server.WlRegionResource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.westmalle.wayland.protocol.WlCompositor;

import javax.media.nativewindow.util.Point;
import javax.media.nativewindow.util.PointImmutable;
import javax.media.nativewindow.util.Rectangle;
import javax.media.nativewindow.util.RectangleImmutable;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
                        //following classes are final, so we have to powermock them:
                        RegionFactory.class
                })
public class SurfaceTest {

    @Mock
    private RegionFactory        regionFactory;
    @Mock
    private WlCompositorResource wlCompositorResource;
    @InjectMocks
    private Surface              surface;

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
        //when
        this.surface.attachBuffer(buffer,
                                  relX,
                                  relY);
        this.surface.commit();
        //then
        assertThat(this.surface.getBuffer()
                               .isPresent()).isTrue();
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
        //when
        this.surface.attachBuffer(buffer0,
                                  relX0,
                                  relY0);
        this.surface.attachBuffer(buffer1,
                                  relX1,
                                  relY1);
        this.surface.commit();
        //then
        //then
        assertThat(this.surface.getBuffer()
                               .get()).isSameAs(buffer1);
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
        //when
        this.surface.attachBuffer(buffer,
                                  relX,
                                  relY);
        this.surface.detachBuffer();
        this.surface.commit();
        //then
        assertThat(this.surface.getBuffer()
                               .isPresent()).isFalse();
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
        assertThat(this.surface.getOpaqueRegion()
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
        assertThat(this.surface.getInputRegion()
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