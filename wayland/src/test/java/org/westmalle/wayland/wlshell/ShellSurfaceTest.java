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
package org.westmalle.wayland.wlshell;

import org.freedesktop.wayland.server.*;
import org.freedesktop.wayland.shared.WlShellSurfaceResize;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.westmalle.wayland.core.*;
import org.westmalle.wayland.core.calc.Mat4;
import org.westmalle.wayland.core.calc.Vec4;
import org.westmalle.wayland.core.events.Motion;
import org.westmalle.wayland.protocol.WlCompositor;
import org.westmalle.wayland.protocol.WlPointer;
import org.westmalle.wayland.protocol.WlSurface;

import java.util.LinkedList;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ShellSurfaceTest {

    @Mock
    private EventSource  eventSource;
    @Mock
    private EventLoop    eventLoop;
    @Mock
    private Display      display;
    @Mock
    private WlCompositor wlCompositor;

    @Before
    public void setUp() {
        when(this.display.getEventLoop()).thenReturn(this.eventLoop);
        when(this.eventLoop.addTimer(any())).thenReturn(this.eventSource);
    }

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
        final WlSurface         wlSurface         = mock(WlSurface.class);
        final Surface           surface           = mock(Surface.class);
        when(wlSurfaceResource.getImplementation()).thenReturn(wlSurface);
        when(wlSurface.getSurface()).thenReturn(surface);
        final Point surfacePosition = Point.create(75,
                                                   75);
        when(surface.global(eq(Point.create(0,
                                            0)))).thenReturn(surfacePosition);

        final ShellSurface shellSurface = new ShellSurface(this.display,
                                                           this.wlCompositor,
                                                           0);
        //when
        shellSurface.move(wlSurfaceResource,
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
        final Point pointerPositionMotion = Point.create(1200,
                                                         900);
        final int serial = 12345;

        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
        final WlSurface         wlSurface         = mock(WlSurface.class);
        when(wlSurfaceResource.getImplementation()).thenReturn(wlSurface);
        final Surface surface = mock(Surface.class);
        when(wlSurface.getSurface()).thenReturn(surface);

        when(surface.local(pointerPositionStart)).thenReturn(Point.create(80,
                                                                          80));
        final Mat4 inverseTransform = mock(Mat4.class);
        when(surface.getInverseTransform()).thenReturn(inverseTransform);
        when(inverseTransform.multiply(pointerPositionMotion.toVec4())).thenReturn(Vec4.create(180,
                                                                                               180,
                                                                                               0,
                                                                                               1));

        when(surface.getSize()).thenReturn(Rectangle.create(0,
                                                            0,
                                                            100,
                                                            100));

        final ShellSurface shellSurface = new ShellSurface(this.display,
                                                           this.wlCompositor,
                                                           0);

        //when
        shellSurface.resize(wlShellSurfaceResource,
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
        final Point pointerPositionMotion = Point.create(1200,
                                                         900);
        final int serial = 12345;

        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
        final WlSurface         wlSurface         = mock(WlSurface.class);
        when(wlSurfaceResource.getImplementation()).thenReturn(wlSurface);
        final Surface surface = mock(Surface.class);
        when(wlSurface.getSurface()).thenReturn(surface);

        when(surface.local(pointerPositionStart)).thenReturn(Point.create(80,
                                                                          80));
        final Mat4 inverseTransform = mock(Mat4.class);
        when(surface.getInverseTransform()).thenReturn(inverseTransform);
        when(inverseTransform.multiply(pointerPositionMotion.toVec4())).thenReturn(Vec4.create(180,
                                                                                               180,
                                                                                               0,
                                                                                               1));

        when(surface.getSize()).thenReturn(Rectangle.create(0,
                                                            0,
                                                            100,
                                                            100));

        final ShellSurface shellSurface = new ShellSurface(this.display,
                                                           this.wlCompositor,
                                                           0);

        //when
        shellSurface.resize(wlShellSurfaceResource,
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
        final Point pointerPositionMotion = Point.create(1200,
                                                         900);
        final int serial = 12345;

        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
        final WlSurface         wlSurface         = mock(WlSurface.class);
        when(wlSurfaceResource.getImplementation()).thenReturn(wlSurface);
        final Surface surface = mock(Surface.class);
        when(wlSurface.getSurface()).thenReturn(surface);

        when(surface.local(pointerPositionStart)).thenReturn(Point.create(80,
                                                                          20));
        final Mat4 inverseTransform = mock(Mat4.class);
        when(surface.getInverseTransform()).thenReturn(inverseTransform);
        when(inverseTransform.multiply(pointerPositionMotion.toVec4())).thenReturn(Vec4.create(180,
                                                                                               -80,
                                                                                               0,
                                                                                               1));

        when(surface.getSize()).thenReturn(Rectangle.create(0,
                                                            0,
                                                            100,
                                                            100));

        final ShellSurface shellSurface = new ShellSurface(this.display,
                                                           this.wlCompositor,
                                                           0);

        //when
        shellSurface.resize(wlShellSurfaceResource,
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
        final Point pointerPositionMotion = Point.create(1200,
                                                         900);
        final int serial = 12345;

        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
        final WlSurface         wlSurface         = mock(WlSurface.class);
        when(wlSurfaceResource.getImplementation()).thenReturn(wlSurface);
        final Surface surface = mock(Surface.class);
        when(wlSurface.getSurface()).thenReturn(surface);

        when(surface.local(pointerPositionStart)).thenReturn(Point.create(80,
                                                                          20));
        final Mat4 inverseTransform = mock(Mat4.class);
        when(surface.getInverseTransform()).thenReturn(inverseTransform);
        when(inverseTransform.multiply(pointerPositionMotion.toVec4())).thenReturn(Vec4.create(180,
                                                                                               -80,
                                                                                               0,
                                                                                               1));

        when(surface.getSize()).thenReturn(Rectangle.create(0,
                                                            0,
                                                            100,
                                                            100));

        final ShellSurface shellSurface = new ShellSurface(this.display,
                                                           this.wlCompositor,
                                                           0);

        //when
        shellSurface.resize(wlShellSurfaceResource,
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
        final Point pointerPositionMotion = Point.create(1200,
                                                         900);
        final int serial = 12345;

        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
        final WlSurface         wlSurface         = mock(WlSurface.class);
        when(wlSurfaceResource.getImplementation()).thenReturn(wlSurface);
        final Surface surface = mock(Surface.class);
        when(wlSurface.getSurface()).thenReturn(surface);

        when(surface.local(pointerPositionStart)).thenReturn(Point.create(20,
                                                                          20));
        final Mat4 inverseTransform = mock(Mat4.class);
        when(surface.getInverseTransform()).thenReturn(inverseTransform);
        when(inverseTransform.multiply(pointerPositionMotion.toVec4())).thenReturn(Vec4.create(-80,
                                                                                               -80,
                                                                                               0,
                                                                                               1));

        when(surface.getSize()).thenReturn(Rectangle.create(0,
                                                            0,
                                                            100,
                                                            100));

        final ShellSurface shellSurface = new ShellSurface(this.display,
                                                           this.wlCompositor,
                                                           0);

        //when
        shellSurface.resize(wlShellSurfaceResource,
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
        final Point pointerPositionMotion = Point.create(1200,
                                                         900);

        final int serial = 12345;

        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
        final WlSurface         wlSurface         = mock(WlSurface.class);
        when(wlSurfaceResource.getImplementation()).thenReturn(wlSurface);
        final Surface surface = mock(Surface.class);
        when(wlSurface.getSurface()).thenReturn(surface);

        when(surface.local(pointerPositionStart)).thenReturn(Point.create(20,
                                                                          20));
        final Mat4 inverseTransform = mock(Mat4.class);
        when(surface.getInverseTransform()).thenReturn(inverseTransform);
        when(inverseTransform.multiply(pointerPositionMotion.toVec4())).thenReturn(Vec4.create(-80,
                                                                                               -80,
                                                                                               0,
                                                                                               1));
        when(surface.getSize()).thenReturn(Rectangle.create(0,
                                                            0,
                                                            100,
                                                            100));

        final ShellSurface shellSurface = new ShellSurface(this.display,
                                                           this.wlCompositor,
                                                           0);

        //when
        shellSurface.resize(wlShellSurfaceResource,
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
        final Point pointerPositionMotion = Point.create(1200,
                                                         900);
        final int serial = 12345;

        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
        final WlSurface         wlSurface         = mock(WlSurface.class);
        when(wlSurfaceResource.getImplementation()).thenReturn(wlSurface);
        final Surface surface = mock(Surface.class);
        when(wlSurface.getSurface()).thenReturn(surface);

        when(surface.local(pointerPositionStart)).thenReturn(Point.create(20,
                                                                          80));
        final Mat4 inverseTransform = mock(Mat4.class);
        when(surface.getInverseTransform()).thenReturn(inverseTransform);
        when(inverseTransform.multiply(pointerPositionMotion.toVec4())).thenReturn(Vec4.create(-80,
                                                                                               180,
                                                                                               0,
                                                                                               1));

        when(surface.getSize()).thenReturn(Rectangle.create(0,
                                                            0,
                                                            100,
                                                            100));

        final ShellSurface shellSurface = new ShellSurface(this.display,
                                                           this.wlCompositor,
                                                           0);

        //when
        shellSurface.resize(wlShellSurfaceResource,
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
        final Point pointerPositionMotion = Point.create(1200,
                                                         900);
        final int serial = 12345;

        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
        final WlSurface         wlSurface         = mock(WlSurface.class);
        when(wlSurfaceResource.getImplementation()).thenReturn(wlSurface);
        final Surface surface = mock(Surface.class);
        when(wlSurface.getSurface()).thenReturn(surface);

        when(surface.local(pointerPositionStart)).thenReturn(Point.create(20,
                                                                          80));
        final Mat4 inverseTransform = mock(Mat4.class);
        when(surface.getInverseTransform()).thenReturn(inverseTransform);
        when(inverseTransform.multiply(pointerPositionMotion.toVec4())).thenReturn(Vec4.create(-80,
                                                                                               180,
                                                                                               0,
                                                                                               1));

        when(surface.getSize()).thenReturn(Rectangle.create(0,
                                                            0,
                                                            100,
                                                            100));

        final ShellSurface shellSurface = new ShellSurface(this.display,
                                                           this.wlCompositor,
                                                           0);

        //when
        shellSurface.resize(wlShellSurfaceResource,
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

    @Test
    public void testPong() {
        //given
        final WlShellSurfaceResource wlShellSurfaceResource = mock(WlShellSurfaceResource.class);
        final int                    pingSerial             = 12345;

        final ShellSurface shellSurface = new ShellSurface(this.display,
                                                           this.wlCompositor,
                                                           12345);

        //when
        shellSurface.pong(wlShellSurfaceResource,
                          pingSerial);

        //then
        verify(wlShellSurfaceResource).ping(pingSerial);
        verify(this.eventSource).updateTimer(anyInt());
    }

    @Test
    public void testPongTimeout() {
        //given
        final ShellSurface shellSurface = new ShellSurface(this.display,
                                                           this.wlCompositor,
                                                           12345);
        final ArgumentCaptor<EventLoop.TimerEventHandler> timerEventHandlerArgumentCaptor = ArgumentCaptor.forClass(EventLoop.TimerEventHandler.class);
        verify(this.eventLoop).addTimer(timerEventHandlerArgumentCaptor.capture());
        final EventLoop.TimerEventHandler timerEventHandler = timerEventHandlerArgumentCaptor.getValue();

        final WlShellSurfaceResource wlShellSurfaceResource = mock(WlShellSurfaceResource.class);
        final int                    pingSerial             = 12345;
        shellSurface.pong(wlShellSurfaceResource,
                          pingSerial);

        //when
        timerEventHandler.handle();

        //then
        assertThat(shellSurface.isActive()).isFalse();
    }

    @Test
    public void testToFront() throws Exception {
        //given
        final ShellSurface shellSurface = new ShellSurface(this.display,
                                                           this.wlCompositor,
                                                           12345);

        final Compositor compositor = mock(Compositor.class);
        when(this.wlCompositor.getCompositor()).thenReturn(compositor);
        final LinkedList<WlSurfaceResource> surfacesStack = new LinkedList<>();
        when(compositor.getSurfacesStack()).thenReturn(surfacesStack);

        final WlSurfaceResource wlSurfaceResource0 = mock(WlSurfaceResource.class);
        final WlSurfaceResource wlSurfaceResource1 = mock(WlSurfaceResource.class);
        final WlSurfaceResource wlSurfaceResource2 = mock(WlSurfaceResource.class);

        surfacesStack.add(wlSurfaceResource1);
        surfacesStack.add(wlSurfaceResource0);
        surfacesStack.add(wlSurfaceResource2);

        //when
        shellSurface.toFront(wlSurfaceResource0);

        //then
        assertThat(surfacesStack.getLast()).isSameAs(wlSurfaceResource0);
    }

    @Test
    public void testSetTransient() throws Exception {
        //given
        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
        final WlSurface         wlSurface         = mock(WlSurface.class);
        when(wlSurfaceResource.getImplementation()).thenReturn(wlSurface);
        final Surface surface = mock(Surface.class);
        when(wlSurface.getSurface()).thenReturn(surface);

        final WlSurfaceResource parentWlSurfaceResource = mock(WlSurfaceResource.class);
        final WlSurface         parentWlSurface         = mock(WlSurface.class);
        when(parentWlSurfaceResource.getImplementation()).thenReturn(parentWlSurface);
        final Surface parentSurface = mock(Surface.class);
        when(parentWlSurface.getSurface()).thenReturn(parentSurface);

        final int localX = 75;
        final int localY = 120;

        final int globalX = 100;
        final int globalY = 150;

        when(parentSurface.global(Point.create(localX,
                                               localY))).thenReturn(Point.create(globalX,
                                                                                 globalY));

        final ShellSurface shellSurface = new ShellSurface(this.display,
                                                           this.wlCompositor,
                                                           12345);

        //when
        shellSurface.setTransient(wlSurfaceResource,
                                  parentWlSurfaceResource,
                                  localX,
                                  localY,
                                  0);

        //then
        verify(surface).setPosition(Point.create(globalX,
                                                 globalY));
    }
}