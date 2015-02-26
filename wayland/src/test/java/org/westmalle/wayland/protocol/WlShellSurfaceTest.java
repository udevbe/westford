package org.westmalle.wayland.protocol;

import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.WlSeatResource;
import org.freedesktop.wayland.server.WlShellSurfaceResource;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.freedesktop.wayland.server.jna.WaylandServerLibrary;
import org.freedesktop.wayland.server.jna.WaylandServerLibraryMapping;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.westmalle.wayland.output.PointerDevice;
import org.westmalle.wayland.output.PointerGrabMotion;
import org.westmalle.wayland.output.Surface;
import org.westmalle.wayland.output.events.Motion;

import javax.media.nativewindow.util.Point;
import java.util.Optional;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
                        //following classes have static methods, so we have to powermock them:
                        WaylandServerLibrary.class
                })
public class WlShellSurfaceTest {

    @Mock
    private WaylandServerLibraryMapping waylandServerLibraryMapping;

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(WaylandServerLibrary.class);
        when(WaylandServerLibrary.INSTANCE()).thenReturn(this.waylandServerLibraryMapping);
    }

    @Test
    public void testMove() throws Exception {
        //given
        final WlShellSurfaceResource wlShellSurfaceResource = mock(WlShellSurfaceResource.class);
        final WlSeatResource wlSeatResource = mock(WlSeatResource.class);
        final WlSeat wlSeat = mock(WlSeat.class);
        when(wlSeatResource.getImplementation()).thenReturn(wlSeat);

        final WlPointer wlPointer = mock(WlPointer.class);
        when(wlSeat.getOptionalWlPointer()).thenReturn(Optional.of(wlPointer));

        final PointerDevice pointerDevice = mock(PointerDevice.class);
        when(wlPointer.getPointerDevice()).thenReturn(pointerDevice);
        final Point pointerPosition = new Point(100,
                                                100);
        when(pointerDevice.getPosition()).thenReturn(pointerPosition);

        final int serial = 12345;

        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
        final WlSurface wlSurface = mock(WlSurface.class);
        final Surface surface = mock(Surface.class);
        when(wlSurfaceResource.getImplementation()).thenReturn(wlSurface);
        when(wlSurface.getSurface()).thenReturn(surface);
        final Point surfacePosition = new Point(75,
                                                75);
        when(surface.getPosition()).thenReturn(surfacePosition);

        final WlShellSurface wlShellSurface = new WlShellSurface(wlSurfaceResource);
        //when
        wlShellSurface.move(wlShellSurfaceResource,
                            wlSeatResource,
                            serial);
        //then
        ArgumentCaptor<PointerGrabMotion> pointerGrabMotionCaptor = ArgumentCaptor.forClass(PointerGrabMotion.class);
        verify(pointerDevice,
               times(1)).grabMotion(eq(wlSurfaceResource),
                                    eq(serial),
                                    pointerGrabMotionCaptor.capture());
        //and when
        final PointerGrabMotion pointerGrabMotion = pointerGrabMotionCaptor.getValue();
        pointerGrabMotion.motion(pointerDevice,
                                 new Motion(98765,
                                            110,
                                            110));
        //then
        verify(surface).setPosition(new Point(85,
                                              85));
    }

    @Test
    public void testCreate() throws Exception {
        //given
        final Client client = mock(Client.class);
        final int version = 1;
        final int id = 1;

        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
        final WlShellSurface wlShellSurface = new WlShellSurface(wlSurfaceResource);

        //when
        final WlShellSurfaceResource wlShellSurfaceResource = wlShellSurface.create(client,
                                                                                    version,
                                                                                    id);
        //then
        assertThat(wlShellSurfaceResource).isNotNull();
    }
}