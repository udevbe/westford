package org.westmalle.wayland.output;

import org.freedesktop.wayland.server.WlCompositorResource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

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
    public void testRemoveTransform() throws Exception {

    }

    @Test
    public void testDetachBuffer() throws Exception {

    }

    @Test
    public void testCommit() throws Exception {

    }

    @Test
    public void testRemoveOpaqueRegion() throws Exception {

    }

    @Test
    public void testRemoveInputRegion() throws Exception {

    }

    @Test
    public void testFirePaintCallbacks() throws Exception {

    }

    @Test
    public void testRelativeCoordinate() throws Exception {

    }
}