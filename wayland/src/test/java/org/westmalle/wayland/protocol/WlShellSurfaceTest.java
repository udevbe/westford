package org.westmalle.wayland.protocol;

import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.WlSeatResource;
import org.freedesktop.wayland.server.WlShellSurfaceResource;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.freedesktop.wayland.server.jna.WaylandServerLibrary;
import org.freedesktop.wayland.server.jna.WaylandServerLibraryMapping;
import org.freedesktop.wayland.shared.WlShellSurfaceResize;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.westmalle.wayland.output.*;
import org.westmalle.wayland.output.events.Motion;

import java.util.Optional;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(WaylandServerLibrary.class)
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
        final Point pointerPosition = Point.create(100,
                                                   100);
        when(pointerDevice.getPosition()).thenReturn(pointerPosition);

        final int serial = 12345;

        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
        final WlSurface wlSurface = mock(WlSurface.class);
        final Surface surface = mock(Surface.class);
        when(wlSurfaceResource.getImplementation()).thenReturn(wlSurface);
        when(wlSurface.getSurface()).thenReturn(surface);
        final Point surfacePosition = Point.create(75,
                                                   75);
        when(surface.getPosition()).thenReturn(surfacePosition);

        final WlShellSurface wlShellSurface = new WlShellSurface(wlSurfaceResource);
        //when
        wlShellSurface.move(wlShellSurfaceResource,
                            wlSeatResource,
                            serial);
        //then
        final ArgumentCaptor<PointerGrabMotion> pointerGrabMotionCaptor = ArgumentCaptor.forClass(PointerGrabMotion.class);
        verify(pointerDevice).grabMotion(eq(wlSurfaceResource),
                                         eq(serial),
                                         pointerGrabMotionCaptor.capture());
        //and when
        final PointerGrabMotion pointerGrabMotion = pointerGrabMotionCaptor.getValue();
        pointerGrabMotion.motion(Motion.create(98765,
                                               110,
                                               110));
        //then
        verify(surface).setPosition(Point.create(85,
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

    @Test
    public void testResizeBottomRight() throws Exception {
        //given
        final WlShellSurfaceResource wlShellSurfaceResource = mock(WlShellSurfaceResource.class);
        final WlSeatResource wlSeatResource = mock(WlSeatResource.class);
        final WlSeat wlSeat = mock(WlSeat.class);
        when(wlSeatResource.getImplementation()).thenReturn(wlSeat);

        final WlPointer wlPointer = mock(WlPointer.class);
        when(wlSeat.getOptionalWlPointer()).thenReturn(Optional.of(wlPointer));

        final PointerDevice pointerDevice = mock(PointerDevice.class);
        when(wlPointer.getPointerDevice()).thenReturn(pointerDevice);
        final Point pointerPositionStart = mock(Point.class);
        when(pointerDevice.getPosition()).thenReturn(pointerPositionStart);
        final Point pointerPositionMotion = mock(Point.class);

        final int serial = 12345;

        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
        final WlSurface wlSurface = mock(WlSurface.class);
        when(wlSurfaceResource.getImplementation()).thenReturn(wlSurface);
        final Surface surface = mock(Surface.class);
        when(wlSurface.getSurface()).thenReturn(surface);

        when(surface.local(pointerPositionStart)).thenReturn(Point.create(80,
                                                                          80));
        when(surface.local(pointerPositionMotion)).thenReturn(Point.create(180,
                                                                           180));

        when(surface.getSize()).thenReturn(Rectangle.create(0,
                                                            0,
                                                            100,
                                                            100));

        final WlShellSurface wlShellSurface = new WlShellSurface(wlSurfaceResource);
        //when
        wlShellSurface.resize(wlShellSurfaceResource,
                              wlSeatResource,
                              serial,
                              0);
        //then
        final ArgumentCaptor<PointerGrabMotion> pointerGrabMotionArgumentCaptor = ArgumentCaptor.forClass(PointerGrabMotion.class);
        verify(pointerDevice).grabMotion(eq(wlSurfaceResource),
                                         eq(serial),
                                         pointerGrabMotionArgumentCaptor.capture());
        //and when
        final PointerGrabMotion pointerGrabMotion = pointerGrabMotionArgumentCaptor.getValue();
        pointerGrabMotion.motion(Motion.create(456767,
                                               pointerPositionMotion));
        //then
        verify(wlShellSurfaceResource).configure(WlShellSurfaceResize.BOTTOM_RIGHT.getValue(),
                                                 200,
                                                 200);
    }

    @Test
    public void testResizeTopRight() throws Exception {
        //given
        final WlShellSurfaceResource wlShellSurfaceResource = mock(WlShellSurfaceResource.class);
        final WlSeatResource wlSeatResource = mock(WlSeatResource.class);
        final WlSeat wlSeat = mock(WlSeat.class);
        when(wlSeatResource.getImplementation()).thenReturn(wlSeat);

        final WlPointer wlPointer = mock(WlPointer.class);
        when(wlSeat.getOptionalWlPointer()).thenReturn(Optional.of(wlPointer));

        final PointerDevice pointerDevice = mock(PointerDevice.class);
        when(wlPointer.getPointerDevice()).thenReturn(pointerDevice);
        final Point pointerPositionStart = mock(Point.class);
        when(pointerDevice.getPosition()).thenReturn(pointerPositionStart);
        final Point pointerPositionMotion = mock(Point.class);

        final int serial = 12345;

        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
        final WlSurface wlSurface = mock(WlSurface.class);
        when(wlSurfaceResource.getImplementation()).thenReturn(wlSurface);
        final Surface surface = mock(Surface.class);
        when(wlSurface.getSurface()).thenReturn(surface);

        when(surface.local(pointerPositionStart)).thenReturn(Point.create(80,
                                                                          20));
        when(surface.local(pointerPositionMotion)).thenReturn(Point.create(180,
                                                                           -80));

        when(surface.getSize()).thenReturn(Rectangle.create(0,
                                                            0,
                                                            100,
                                                            100));

        final WlShellSurface wlShellSurface = new WlShellSurface(wlSurfaceResource);
        //when
        wlShellSurface.resize(wlShellSurfaceResource,
                              wlSeatResource,
                              serial,
                              0);
        //then
        final ArgumentCaptor<PointerGrabMotion> pointerGrabMotionArgumentCaptor = ArgumentCaptor.forClass(PointerGrabMotion.class);
        verify(pointerDevice).grabMotion(eq(wlSurfaceResource),
                                         eq(serial),
                                         pointerGrabMotionArgumentCaptor.capture());
        //and when
        final PointerGrabMotion pointerGrabMotion = pointerGrabMotionArgumentCaptor.getValue();
        pointerGrabMotion.motion(Motion.create(456767,
                                               pointerPositionMotion));
        //then
        verify(wlShellSurfaceResource).configure(WlShellSurfaceResize.TOP_RIGHT.getValue(),
                                                 200,
                                                 200);
    }

    @Test
    public void testResizeTopLeft() throws Exception {
        //TODO test other corners
        //given
        final WlShellSurfaceResource wlShellSurfaceResource = mock(WlShellSurfaceResource.class);
        final WlSeatResource wlSeatResource = mock(WlSeatResource.class);
        final WlSeat wlSeat = mock(WlSeat.class);
        when(wlSeatResource.getImplementation()).thenReturn(wlSeat);

        final WlPointer wlPointer = mock(WlPointer.class);
        when(wlSeat.getOptionalWlPointer()).thenReturn(Optional.of(wlPointer));

        final PointerDevice pointerDevice = mock(PointerDevice.class);
        when(wlPointer.getPointerDevice()).thenReturn(pointerDevice);
        final Point pointerPositionStart = mock(Point.class);
        when(pointerDevice.getPosition()).thenReturn(pointerPositionStart);
        final Point pointerPositionMotion = mock(Point.class);

        final int serial = 12345;

        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
        final WlSurface wlSurface = mock(WlSurface.class);
        when(wlSurfaceResource.getImplementation()).thenReturn(wlSurface);
        final Surface surface = mock(Surface.class);
        when(wlSurface.getSurface()).thenReturn(surface);

        when(surface.local(pointerPositionStart)).thenReturn(Point.create(20,
                                                                          20));
        when(surface.local(pointerPositionMotion)).thenReturn(Point.create(-80,
                                                                           -80));

        when(surface.getSize()).thenReturn(Rectangle.create(0,
                                                            0,
                                                            100,
                                                            100));

        final WlShellSurface wlShellSurface = new WlShellSurface(wlSurfaceResource);
        //when
        wlShellSurface.resize(wlShellSurfaceResource,
                              wlSeatResource,
                              serial,
                              0);
        //then
        final ArgumentCaptor<PointerGrabMotion> pointerGrabMotionArgumentCaptor = ArgumentCaptor.forClass(PointerGrabMotion.class);
        verify(pointerDevice).grabMotion(eq(wlSurfaceResource),
                                         eq(serial),
                                         pointerGrabMotionArgumentCaptor.capture());
        //and when
        final PointerGrabMotion pointerGrabMotion = pointerGrabMotionArgumentCaptor.getValue();
        pointerGrabMotion.motion(Motion.create(456767,
                                               pointerPositionMotion));
        //then
        verify(wlShellSurfaceResource).configure(WlShellSurfaceResize.TOP_LEFT.getValue(),
                                                 200,
                                                 200);
    }

    @Test
    public void testResizeBottomLeft() throws Exception {
        //given
        final WlShellSurfaceResource wlShellSurfaceResource = mock(WlShellSurfaceResource.class);
        final WlSeatResource wlSeatResource = mock(WlSeatResource.class);
        final WlSeat wlSeat = mock(WlSeat.class);
        when(wlSeatResource.getImplementation()).thenReturn(wlSeat);

        final WlPointer wlPointer = mock(WlPointer.class);
        when(wlSeat.getOptionalWlPointer()).thenReturn(Optional.of(wlPointer));

        final PointerDevice pointerDevice = mock(PointerDevice.class);
        when(wlPointer.getPointerDevice()).thenReturn(pointerDevice);
        final Point pointerPositionStart = mock(Point.class);
        when(pointerDevice.getPosition()).thenReturn(pointerPositionStart);
        final Point pointerPositionMotion = mock(Point.class);

        final int serial = 12345;

        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
        final WlSurface wlSurface = mock(WlSurface.class);
        when(wlSurfaceResource.getImplementation()).thenReturn(wlSurface);
        final Surface surface = mock(Surface.class);
        when(wlSurface.getSurface()).thenReturn(surface);

        when(surface.local(pointerPositionStart)).thenReturn(Point.create(20,
                                                                          80));
        when(surface.local(pointerPositionMotion)).thenReturn(Point.create(-80,
                                                                           180));

        when(surface.getSize()).thenReturn(Rectangle.create(0,
                                                            0,
                                                            100,
                                                            100));

        final WlShellSurface wlShellSurface = new WlShellSurface(wlSurfaceResource);
        //when
        wlShellSurface.resize(wlShellSurfaceResource,
                              wlSeatResource,
                              serial,
                              0);
        //then
        final ArgumentCaptor<PointerGrabMotion> pointerGrabMotionArgumentCaptor = ArgumentCaptor.forClass(PointerGrabMotion.class);
        verify(pointerDevice).grabMotion(eq(wlSurfaceResource),
                                         eq(serial),
                                         pointerGrabMotionArgumentCaptor.capture());
        //and when
        final PointerGrabMotion pointerGrabMotion = pointerGrabMotionArgumentCaptor.getValue();
        pointerGrabMotion.motion(Motion.create(456767,
                                               pointerPositionMotion));
        //then
        verify(wlShellSurfaceResource).configure(WlShellSurfaceResize.BOTTOM_LEFT.getValue(),
                                                 200,
                                                 200);
    }
}