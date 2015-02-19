package org.westmalle.wayland.output;

import org.freedesktop.wayland.server.WlSurfaceResource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.westmalle.wayland.protocol.WlSurface;

import javax.media.nativewindow.util.Point;
import javax.media.nativewindow.util.PointImmutable;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SceneTest {

    @InjectMocks
    private Scene scene;

    @Test
    public void testRelativeCoordinate() throws Exception {
        //given
        final WlSurfaceResource surfaceResource = mock(WlSurfaceResource.class);

        final WlSurface implementation = mock(WlSurface.class);
        when(surfaceResource.getImplementation()).thenReturn(implementation);

        final Surface surface = mock(Surface.class);
        when(implementation.getSurface()).thenReturn(surface);

        final PointImmutable surfacePosition = new Point(70,
                                                         80);
        when(surface.getPosition()).thenReturn(surfacePosition);

        final PointImmutable absPosition = new Point(100,
                                                     100);
        //when
        final PointImmutable relativeCoordinate = this.scene.relativeCoordinate(surfaceResource,
                                                                                absPosition);
        //then
        assertThat(relativeCoordinate).isEqualTo(new Point(30,
                                                           20));
    }

    @Test
    public void testFindSurfaceAtCoordinate() throws Exception {
        //given
        final PointImmutable absPosition = new Point(50,
                                                     50);

    }
}