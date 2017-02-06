package org.westford.compositor.core;

import org.freedesktop.wayland.server.WlBufferResource;
import org.freedesktop.wayland.server.WlRegionResource;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.westford.compositor.protocol.WlRegion;
import org.westford.compositor.protocol.WlSurface;

import java.util.Collections;
import java.util.Optional;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SceneTest {

    @Mock
    private InfiniteRegion infiniteRegion;
    @InjectMocks
    private Scene          scene;

    @Test
    public void pickSurface() throws Exception {
        //given
        final Point global = mock(Point.class);

        final WlSurfaceResource wlSurfaceResource0 = mock(WlSurfaceResource.class);
        final WlSurface         wlSurface0         = mock(WlSurface.class);
        when(wlSurfaceResource0.getImplementation()).thenReturn(wlSurface0);
        final Surface surface0 = mock(Surface.class);
        when(wlSurface0.getSurface()).thenReturn(surface0);
        final SurfaceView surfaceView0 = mock(SurfaceView.class);
        when(surface0.getViews()).thenReturn(Collections.singleton(surfaceView0));
        final Rectangle size0 = mock(Rectangle.class);
        when(surface0.getSize()).thenReturn(size0);
        final SurfaceState surfaceState0 = mock(SurfaceState.class);
        when(surface0.getState()).thenReturn(surfaceState0);
        final WlBufferResource wlBufferResource0 = mock(WlBufferResource.class);
        when(surfaceState0.getBuffer()).thenReturn(Optional.of(wlBufferResource0));
        final WlRegionResource wlRegionResource0 = mock(WlRegionResource.class);
        final WlRegion         wlRegion0         = mock(WlRegion.class);
        when(wlRegionResource0.getImplementation()).thenReturn(wlRegion0);
        final Region region0 = mock(Region.class);
        when(wlRegion0.getRegion()).thenReturn(region0);
        when(surfaceState0.getInputRegion()).thenReturn(Optional.of(region0));
        final Point position0 = mock(Point.class);
        when(surfaceView0.local(global)).thenReturn(position0);
        when(region0.contains(size0,
                              position0)).thenReturn(true);

        final WlSurfaceResource wlSurfaceResource1 = mock(WlSurfaceResource.class);
        final WlSurface         wlSurface1         = mock(WlSurface.class);
        when(wlSurfaceResource1.getImplementation()).thenReturn(wlSurface1);
        final Surface surface1 = mock(Surface.class);
        when(wlSurface1.getSurface()).thenReturn(surface1);
        final SurfaceView surfaceView1 = mock(SurfaceView.class);
        when(surface1.getViews()).thenReturn(Collections.singleton(surfaceView1));
        final Rectangle size1 = mock(Rectangle.class);
        when(surface1.getSize()).thenReturn(size1);
        final SurfaceState surfaceState1 = mock(SurfaceState.class);
        when(surface1.getState()).thenReturn(surfaceState1);
        final WlBufferResource wlBufferResource1 = mock(WlBufferResource.class);
        when(surfaceState1.getBuffer()).thenReturn(Optional.of(wlBufferResource1));
        final WlRegionResource wlRegionResource1 = mock(WlRegionResource.class);
        final WlRegion         wlRegion1         = mock(WlRegion.class);
        when(wlRegionResource1.getImplementation()).thenReturn(wlRegion1);
        final Region region1 = mock(Region.class);
        when(surfaceState1.getInputRegion()).thenReturn(Optional.of(region1));
        when(wlRegion1.getRegion()).thenReturn(region1);
        final Point position1 = mock(Point.class);
        when(surfaceView1.local(global)).thenReturn(position1);
        when(region1.contains(size1,
                              position1)).thenReturn(false);

        this.scene.getSurfacesStack()
                  .add(wlSurfaceResource0);
        this.scene.getSurfacesStack()
                  .add(wlSurfaceResource1);

        //when
        final Optional<SurfaceView> pickSurface = this.scene.pickSurfaceView(global);

        //then
        assertThat(pickSurface.get()).isEqualTo(wlSurfaceResource0);
    }

    //TODO test create surface view stack
}