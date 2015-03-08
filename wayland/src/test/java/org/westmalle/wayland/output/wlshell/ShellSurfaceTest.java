package org.westmalle.wayland.output.wlshell;

import org.freedesktop.wayland.server.WlShellSurfaceResource;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.freedesktop.wayland.shared.WlShellSurfaceResize;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.westmalle.wayland.output.*;
import org.westmalle.wayland.output.events.Motion;
import org.westmalle.wayland.protocol.WlPointer;
import org.westmalle.wayland.protocol.WlSurface;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ShellSurfaceTest {

    @InjectMocks
    private ShellSurface shellSurface;

    @Test
    public void testMove() throws Exception {
        //given
        final WlPointer wlPointer = mock(WlPointer.class);

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

        //when
        this.shellSurface.move(wlSurfaceResource,
                               wlPointer,
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
    public void testResizeRight() throws Exception {
        //given
        final WlShellSurfaceResource wlShellSurfaceResource = mock(WlShellSurfaceResource.class);

        final WlPointer wlPointer = mock(WlPointer.class);

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
        //when
        this.shellSurface.resize(wlShellSurfaceResource,
                                 wlSurfaceResource,
                                 wlPointer,
                                 serial,
                                 WlShellSurfaceResize.RIGHT.getValue());
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
        verify(wlShellSurfaceResource).configure(WlShellSurfaceResize.RIGHT.getValue(),
                                                 200,
                                                 100);
    }

    @Test
    public void testResizeBottomRight() throws Exception {
        //given
        final WlShellSurfaceResource wlShellSurfaceResource = mock(WlShellSurfaceResource.class);

        final WlPointer wlPointer = mock(WlPointer.class);

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
        //when
        this.shellSurface.resize(wlShellSurfaceResource,
                                 wlSurfaceResource,
                                 wlPointer,
                                 serial,
                                 WlShellSurfaceResize.BOTTOM_RIGHT.getValue());
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
    public void testResizeTop() throws Exception {
        //given
        final WlShellSurfaceResource wlShellSurfaceResource = mock(WlShellSurfaceResource.class);

        final WlPointer wlPointer = mock(WlPointer.class);

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
        //when
        this.shellSurface.resize(wlShellSurfaceResource,
                                 wlSurfaceResource,
                                 wlPointer,
                                 serial,
                                 WlShellSurfaceResize.TOP.getValue());
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
        verify(wlShellSurfaceResource).configure(WlShellSurfaceResize.TOP.getValue(),
                                                 100,
                                                 200);
    }

    @Test
    public void testResizeTopRight() throws Exception {
        //given
        final WlShellSurfaceResource wlShellSurfaceResource = mock(WlShellSurfaceResource.class);

        final WlPointer wlPointer = mock(WlPointer.class);

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
        //when
        this.shellSurface.resize(wlShellSurfaceResource,
                                 wlSurfaceResource,
                                 wlPointer,
                                 serial,
                                 WlShellSurfaceResize.TOP_RIGHT.getValue());
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
    public void testResizeLeft() throws Exception {
        //given
        final WlShellSurfaceResource wlShellSurfaceResource = mock(WlShellSurfaceResource.class);

        final WlPointer wlPointer = mock(WlPointer.class);

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
        //when
        this.shellSurface.resize(wlShellSurfaceResource,
                                 wlSurfaceResource,
                                 wlPointer,
                                 serial,
                                 WlShellSurfaceResize.LEFT.getValue());
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
        verify(wlShellSurfaceResource).configure(WlShellSurfaceResize.LEFT.getValue(),
                                                 200,
                                                 100);
    }

    @Test
    public void testResizeTopLeft() throws Exception {
        //given
        final WlShellSurfaceResource wlShellSurfaceResource = mock(WlShellSurfaceResource.class);

        final WlPointer wlPointer = mock(WlPointer.class);

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
        //when
        this.shellSurface.resize(wlShellSurfaceResource,
                                 wlSurfaceResource,
                                 wlPointer,
                                 serial,
                                 WlShellSurfaceResize.TOP_LEFT.getValue());
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
    public void testResizeBottom() throws Exception {
        //given
        final WlShellSurfaceResource wlShellSurfaceResource = mock(WlShellSurfaceResource.class);

        final WlPointer wlPointer = mock(WlPointer.class);

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
        //when
        this.shellSurface.resize(wlShellSurfaceResource,
                                 wlSurfaceResource,
                                 wlPointer,
                                 serial,
                                 WlShellSurfaceResize.BOTTOM.getValue());
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
        verify(wlShellSurfaceResource).configure(WlShellSurfaceResize.BOTTOM.getValue(),
                                                 100,
                                                 200);
    }

    @Test
    public void testResizeBottomLeft() throws Exception {
        //given
        final WlShellSurfaceResource wlShellSurfaceResource = mock(WlShellSurfaceResource.class);

        final WlPointer wlPointer = mock(WlPointer.class);

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
        //when
        this.shellSurface.resize(wlShellSurfaceResource,
                                 wlSurfaceResource,
                                 wlPointer,
                                 serial,
                                 WlShellSurfaceResize.BOTTOM_LEFT.getValue());
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