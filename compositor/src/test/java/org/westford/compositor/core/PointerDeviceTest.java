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

import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.DestroyListener;
import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.WlBufferResource;
import org.freedesktop.wayland.server.WlPointerResource;
import org.freedesktop.wayland.server.WlRegionResource;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.freedesktop.wayland.server.jaccall.WaylandServerCore;
import org.freedesktop.wayland.shared.WlPointerButtonState;
import org.freedesktop.wayland.util.Fixed;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.westford.compositor.core.calc.Geo;
import org.westford.compositor.core.events.PointerMotion;
import org.westford.compositor.protocol.WlRegion;
import org.westford.compositor.protocol.WlSurface;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.powermock.api.mockito.PowerMockito.verifyNoMoreInteractions;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({WaylandServerCore.class,
                 //following classes are final, so we have to powermock them
                 CursorFactory.class})
public class PointerDeviceTest {

    @Mock
    private Geo           geo;
    @Mock
    private Display       display;
    @Mock
    private NullRegion    nullRegion;
    @Mock
    private CursorFactory cursorFactory;
    @Mock
    private JobExecutor   jobExecutor;
    @Mock
    private Scene         scene;
    @Mock
    private FiniteRegion  region;

    @InjectMocks
    private PointerDevice  pointerDevice;

    @Before
    public void setUp() {
        PowerMockito.mockStatic(WaylandServerCore.class);
        Mockito.when(WaylandServerCore.INSTANCE())
               .thenReturn(mock(WaylandServerCore.class));
        when(this.geo.clamp(any(),
                            any(),
                            any())).thenAnswer(invocation -> invocation.getArguments()[1]);
    }

    /**
     * cursor moves from one surface to another surface while button is pressed
     */
    @Test
    public void testGrabNewFocusMotion() throws Exception {
        //given

        //time
        final int time0 = 112358;
        final int time1 = 112459;
        final int time2 = 112712;

        //pointer position 0
        final int x0 = 20;
        final int y0 = 30;
        final Point pointerPos0 = Point.create(x0,
                                               y0);

        //pointer position 1
        final int x1 = 500;
        final int y1 = 600;
        final Point pointerPos1 = Point.create(x1,
                                               y1);

        //mock compositor

        //mock surface 0
        final WlSurfaceResource wlSurfaceResource0 = mock(WlSurfaceResource.class);
        final Client client0 = mock(Client.class);
        when(wlSurfaceResource0.getClient()).thenReturn(client0);
        final WlSurface wlSurface0 = mock(WlSurface.class);
        when(wlSurfaceResource0.getImplementation()).thenReturn(wlSurface0);
        final Surface surface0 = mock(Surface.class);
        when(wlSurface0.getSurface()).thenReturn(surface0);
        final SurfaceView       surfaceView0  = mock(SurfaceView.class);
        Collection<SurfaceView> surfaceViews0 = new LinkedList<>();
        surfaceViews0.add(surfaceView0);
        when(surface0.getViews()).thenReturn(surfaceViews0);

        //mock surface 0 local coordinates
        final Point localPointerPosition0Start = Point.create(1,
                                                              1);
        when(surfaceView0.local(this.pointerDevice.getPosition())).thenReturn(localPointerPosition0Start);
        final Point localPointerPosition00 = Point.create(2,
                                                          3);
        when(surfaceView0.local(eq(pointerPos0))).thenReturn(localPointerPosition00);
        final Point localPointerPosition01 = Point.create(5,
                                                          8);
        when(surfaceView0.local(eq(pointerPos1))).thenReturn(localPointerPosition01);

        when(surfaceView0.getWlSurfaceResource()).thenReturn(wlSurfaceResource0);

        //mock surface 1
        final WlSurfaceResource wlSurfaceResource1 = mock(WlSurfaceResource.class);
        final Client client1 = mock(Client.class);
        when(wlSurfaceResource1.getClient()).thenReturn(client1);
        final WlSurface wlSurface1 = mock(WlSurface.class);
        when(wlSurfaceResource1.getImplementation()).thenReturn(wlSurface1);
        final Surface surface1 = mock(Surface.class);
        when(wlSurface1.getSurface()).thenReturn(surface1);
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
        when(wlRegion1.getRegion()).thenReturn(region1);
        when(surfaceState1.getInputRegion()).thenReturn(Optional.of(region1));
        final SurfaceView       surfaceView1  = mock(SurfaceView.class);
        Collection<SurfaceView> surfaceViews1 = new LinkedList<>();
        surfaceViews1.add(surfaceView1);
        when(surface1.getViews()).thenReturn(surfaceViews1);

        //mock surface 1 local coordinates
        final Point localPointerPosition1Start = Point.create(13,
                                                              21);
        when(surfaceView1.local(this.pointerDevice.getPosition())).thenReturn(localPointerPosition1Start);
        when(region1.contains(size1,
                              localPointerPosition1Start)).thenReturn(false);
        final Point localPointerPosition10 = Point.create(34,
                                                          55);
        when(surfaceView1.local(eq(pointerPos1))).thenReturn(localPointerPosition10);
        when(region1.contains(eq(size1),
                              eq(localPointerPosition10))).thenReturn(false);
        final Point localPointerPosition11 = Point.create(89,
                                                          144);
        when(surfaceView1.local(eq(pointerPos1))).thenReturn(localPointerPosition11);
        when(region1.contains(eq(size1),
                              eq(localPointerPosition11))).thenReturn(true);

        when(surfaceView1.getWlSurfaceResource()).thenReturn(wlSurfaceResource1);

        final Set<WlPointerResource> pointerResources = new HashSet<>();

        //mock pointer 0 resource
        final WlPointerResource wlPointerResource0 = mock(WlPointerResource.class);
        when(wlPointerResource0.getClient()).thenReturn(client0);
        pointerResources.add(wlPointerResource0);

        //mock pointer 1 resource
        final WlPointerResource wlPointerResource1 = mock(WlPointerResource.class);
        when(wlPointerResource1.getClient()).thenReturn(client1);
        pointerResources.add(wlPointerResource1);

        //mock button
        final int button0 = 1;

        //mock display
        final int serial = 90879;
        when(this.display.nextSerial()).thenReturn(serial);

        //mock scene
        when(this.scene.pickSurfaceView(eq(Point.create(x0,
                                                        y0)))).thenReturn(Optional.of(surfaceView0));
        when(this.scene.pickSurfaceView(eq(Point.create(x1,
                                                        y1)))).thenReturn(Optional.of(surfaceView1));

        //when
        this.pointerDevice.motion(pointerResources,
                                  time0,
                                  x0,
                                  y0);
        this.pointerDevice.button(pointerResources,
                                  time1,
                                  button0,
                                  WlPointerButtonState.PRESSED);
        this.pointerDevice.motion(pointerResources,
                                  time2,
                                  x1,
                                  y1);
        //then
        verify(wlPointerResource0).enter(eq(this.display.nextSerial()),
                                         eq(wlSurfaceResource0),
                                         eq(Fixed.create(2)),
                                         eq(Fixed.create(3)));

        verify(wlPointerResource0).motion(eq(time0),
                                          eq(Fixed.create(2)),
                                          eq(Fixed.create(3)));
        verify(wlPointerResource0).motion(eq(time2),
                                          eq(Fixed.create(5)),
                                          eq(Fixed.create(8)));

        verify(wlPointerResource0).button(serial,
                                          time1,
                                          button0,
                                          WlPointerButtonState.PRESSED.value);

        verify(wlPointerResource1,
               never()).enter(anyInt(),
                              any(),
                              any(),
                              any());
        verify(wlPointerResource1,
               never()).button(anyInt(),
                               anyInt(),
                               anyInt(),
                               anyInt());
        verify(wlPointerResource1,
               never()).motion(anyInt(),
                               any(),
                               any());
    }

    /**
     * cursor moves from one surface to another surface, no button is pressed
     */
    @Test
    public void testNoGrabNewFocusMotion() throws Exception {
        //given

        //time
        final int time0 = 112358;
        final int time1 = 112459;

        //pointer position 0
        final int x0 = 20;
        final int y0 = 30;
        final Point pointerPos0 = Point.create(x0,
                                               y0);

        //pointer position 1
        final int x1 = 500;
        final int y1 = 600;
        final Point pointerPos1 = Point.create(x1,
                                               y1);

        //mock compositor

        //mock surface 0
        final WlSurfaceResource wlSurfaceResource0 = mock(WlSurfaceResource.class);
        final Client client0 = mock(Client.class);
        when(wlSurfaceResource0.getClient()).thenReturn(client0);
        final WlSurface wlSurface0 = mock(WlSurface.class);
        when(wlSurfaceResource0.getImplementation()).thenReturn(wlSurface0);
        final Surface surface0 = mock(Surface.class);
        when(wlSurface0.getSurface()).thenReturn(surface0);
        final SurfaceView       surfaceView0  = mock(SurfaceView.class);
        Collection<SurfaceView> surfaceViews0 = new LinkedList<>();
        surfaceViews0.add(surfaceView0);
        when(surface0.getViews()).thenReturn(surfaceViews0);

        //mock surface 0 local coordinates
        final Point localPointerPosition0Start = Point.create(1,
                                                              1);
        when(surfaceView0.local(this.pointerDevice.getPosition())).thenReturn(localPointerPosition0Start);
        final Point localPointerPosition00 = Point.create(2,
                                                          3);
        when(surfaceView0.local(eq(pointerPos0))).thenReturn(localPointerPosition00);
        final Point localPointerPosition01 = Point.create(5,
                                                          8);
        when(surfaceView0.local(eq(pointerPos1))).thenReturn(localPointerPosition01);

        when(surfaceView0.getWlSurfaceResource()).thenReturn(wlSurfaceResource0);

        //mock surface 1
        final WlSurfaceResource wlSurfaceResource1 = mock(WlSurfaceResource.class);
        final Client client1 = mock(Client.class);
        when(wlSurfaceResource1.getClient()).thenReturn(client1);
        final WlSurface wlSurface1 = mock(WlSurface.class);
        when(wlSurfaceResource1.getImplementation()).thenReturn(wlSurface1);
        final Surface surface1 = mock(Surface.class);
        when(wlSurface1.getSurface()).thenReturn(surface1);
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
        when(wlRegion1.getRegion()).thenReturn(region1);
        when(surfaceState1.getInputRegion()).thenReturn(Optional.of(region1));
        final SurfaceView       surfaceView1  = mock(SurfaceView.class);
        Collection<SurfaceView> surfaceViews1 = new LinkedList<>();
        surfaceViews1.add(surfaceView1);
        when(surface1.getViews()).thenReturn(surfaceViews1);

        //mock surface 1 local coordinates
        final Point localPointerPosition1Start = Point.create(13,
                                                              21);
        when(surfaceView1.local(this.pointerDevice.getPosition())).thenReturn(localPointerPosition1Start);
        when(region1.contains(size1,
                              localPointerPosition1Start)).thenReturn(false);
        final Point localPointerPosition10 = Point.create(34,
                                                          55);
        when(surfaceView1.local(eq(pointerPos1))).thenReturn(localPointerPosition10);
        when(region1.contains(eq(size1),
                              eq(localPointerPosition10))).thenReturn(false);
        final Point localPointerPosition11 = Point.create(89,
                                                          144);
        when(surfaceView1.local(eq(pointerPos1))).thenReturn(localPointerPosition11);
        when(region1.contains(eq(size1),
                              eq(localPointerPosition11))).thenReturn(true);

        when(surfaceView1.getWlSurfaceResource()).thenReturn(wlSurfaceResource1);

        final Set<WlPointerResource> pointerResources = new HashSet<>();

        //mock pointer 0 resource
        final WlPointerResource wlPointerResource0 = mock(WlPointerResource.class);
        when(wlPointerResource0.getClient()).thenReturn(client0);
        pointerResources.add(wlPointerResource0);

        //mock pointer 1 resource
        final WlPointerResource wlPointerResource1 = mock(WlPointerResource.class);
        when(wlPointerResource1.getClient()).thenReturn(client1);
        pointerResources.add(wlPointerResource1);

        //mock display
        final int serial0 = 90879;
        final int serial1 = 90881;
        final int serial2 = 90882;
        final int serial3 = 90883;
        when(this.display.nextSerial()).thenReturn(serial0,
                                                   serial1,
                                                   serial2,
                                                   serial3);

        //mock scene
        when(this.scene.pickSurfaceView(eq(Point.create(x0,
                                                        y0)))).thenReturn(Optional.of(surfaceView0));
        when(this.scene.pickSurfaceView(eq(Point.create(x1,
                                                        y1)))).thenReturn(Optional.of(surfaceView1));

        //when
        this.pointerDevice.motion(pointerResources,
                                  time0,
                                  x0,
                                  y0);
        this.pointerDevice.motion(pointerResources,
                                  time1,
                                  x1,
                                  y1);
        //then
        verify(wlPointerResource0).enter(eq(serial0),
                                         eq(wlSurfaceResource0),
                                         eq(Fixed.create(2)),
                                         eq(Fixed.create(3)));

        verify(wlPointerResource0).motion(eq(time0),
                                          eq(Fixed.create(2)),
                                          eq(Fixed.create(3)));

        verify(wlPointerResource0).leave(serial1,
                                         wlSurfaceResource0);

        verify(wlPointerResource1).enter(eq(serial2),
                                         eq(wlSurfaceResource1),
                                         eq(Fixed.create(89)),
                                         eq(Fixed.create(144)));

        verify(wlPointerResource1).motion(eq(time1),
                                          eq(Fixed.create(89)),
                                          eq(Fixed.create(144)));
    }

    /**
     * cursor moves from surface to no surface, no button is pressed
     */
    @Test
    public void testNoFocusMotion() throws Exception {
        //given

        //time
        final int time0 = 112358;
        final int time1 = 112459;

        //pointer position 0
        final int x0 = 20;
        final int y0 = 30;
        final Point pointerPos0 = Point.create(x0,
                                               y0);

        //pointer position 1
        final int x1 = 500;
        final int y1 = 600;
        final Point pointerPos1 = Point.create(x1,
                                               y1);

        //mock surface 0
        final WlSurfaceResource wlSurfaceResource0 = mock(WlSurfaceResource.class);
        final Client client0 = mock(Client.class);
        when(wlSurfaceResource0.getClient()).thenReturn(client0);
        final WlSurface wlSurface0 = mock(WlSurface.class);
        when(wlSurfaceResource0.getImplementation()).thenReturn(wlSurface0);
        final Surface surface0 = mock(Surface.class);
        when(wlSurface0.getSurface()).thenReturn(surface0);
        final SurfaceView       surfaceView0  = mock(SurfaceView.class);
        Collection<SurfaceView> surfaceViews0 = new LinkedList<>();
        surfaceViews0.add(surfaceView0);
        when(surface0.getViews()).thenReturn(surfaceViews0);


        //mock surface 0 local coordinates
        final Point localPointerPosition0Start = Point.create(1,
                                                              1);
        when(surfaceView0.local(this.pointerDevice.getPosition())).thenReturn(localPointerPosition0Start);
        final Point localPointerPosition00 = Point.create(2,
                                                          3);
        when(surfaceView0.local(eq(pointerPos0))).thenReturn(localPointerPosition00);
        final Point localPointerPosition01 = Point.create(5,
                                                          8);
        when(surfaceView0.local(eq(pointerPos1))).thenReturn(localPointerPosition01);

        when(surfaceView0.getWlSurfaceResource()).thenReturn(wlSurfaceResource0);

        final Set<WlPointerResource> pointerResources = new HashSet<>();

        //mock pointer 0 resource
        final WlPointerResource wlPointerResource0 = mock(WlPointerResource.class);
        when(wlPointerResource0.getClient()).thenReturn(client0);
        pointerResources.add(wlPointerResource0);

        //mock display
        final int serial0 = 90879;
        final int serial1 = 90881;
        final int serial2 = 90882;
        when(this.display.nextSerial()).thenReturn(serial0,
                                                   serial1,
                                                   serial2);

        //mock scene
        when(this.scene.pickSurfaceView(eq(Point.create(x0,
                                                        y0)))).thenReturn(Optional.of(surfaceView0));
        when(this.scene.pickSurfaceView(eq(Point.create(x1,
                                                        y1)))).thenReturn(Optional.empty());

        //when
        this.pointerDevice.motion(pointerResources,
                                  time0,
                                  x0,
                                  y0);
        this.pointerDevice.motion(pointerResources,
                                  time1,
                                  x1,
                                  y1);
        //then
        verify(wlPointerResource0).enter(eq(serial0),
                                         eq(wlSurfaceResource0),
                                         eq(Fixed.create(2)),
                                         eq(Fixed.create(3)));

        verify(wlPointerResource0).motion(eq(time0),
                                          eq(Fixed.create(2)),
                                          eq(Fixed.create(3)));

        verify(wlPointerResource0).leave(serial1,
                                         wlSurfaceResource0);
    }

    /**
     * cursor moves from no surface into surface, nu button is pressed
     */
    @Test
    public void testNewFocusMotion() throws Exception {
        //given

        //time
        final int time0 = 112358;
        final int time1 = 112459;

        //pointer position 0
        final int x0 = 20;
        final int y0 = 30;
        final Point pointerPos0 = Point.create(x0,
                                               y0);

        //pointer position 1
        final int x1 = 500;
        final int y1 = 600;
        final Point pointerPos1 = Point.create(x1,
                                               y1);

        //mock surface 0
        final WlSurfaceResource wlSurfaceResource0 = mock(WlSurfaceResource.class);
        final Client client0 = mock(Client.class);
        when(wlSurfaceResource0.getClient()).thenReturn(client0);
        final WlSurface wlSurface0 = mock(WlSurface.class);
        when(wlSurfaceResource0.getImplementation()).thenReturn(wlSurface0);
        final Surface surface0 = mock(Surface.class);
        when(wlSurface0.getSurface()).thenReturn(surface0);
        final SurfaceView       surfaceView0  = mock(SurfaceView.class);
        Collection<SurfaceView> surfaceViews0 = new LinkedList<>();
        surfaceViews0.add(surfaceView0);
        when(surface0.getViews()).thenReturn(surfaceViews0);


        //mock surface 0 local coordinates
        final Point localPointerPosition0Start = Point.create(1,
                                                              1);
        when(surfaceView0.local(this.pointerDevice.getPosition())).thenReturn(localPointerPosition0Start);
        final Point localPointerPosition00 = Point.create(2,
                                                          3);
        when(surfaceView0.local(eq(pointerPos0))).thenReturn(localPointerPosition00);
        final Point localPointerPosition01 = Point.create(5,
                                                          8);
        when(surfaceView0.local(eq(pointerPos1))).thenReturn(localPointerPosition01);

        when(surfaceView0.getWlSurfaceResource()).thenReturn(wlSurfaceResource0);

        final Set<WlPointerResource> pointerResources = new HashSet<>();

        //mock pointer 0 resource
        final WlPointerResource wlPointerResource0 = mock(WlPointerResource.class);
        when(wlPointerResource0.getClient()).thenReturn(client0);
        pointerResources.add(wlPointerResource0);

        //mock display
        final int serial0 = 90879;
        final int serial1 = 90881;
        final int serial2 = 90882;
        when(this.display.nextSerial()).thenReturn(serial0,
                                                   serial1,
                                                   serial2);

        //mock scene
        when(this.scene.pickSurfaceView(eq(Point.create(x0,
                                                        y0)))).thenReturn(Optional.empty());
        when(this.scene.pickSurfaceView(eq(Point.create(x1,
                                                        y1)))).thenReturn(Optional.of(surfaceView0));

        //when
        this.pointerDevice.motion(pointerResources,
                                  time0,
                                  x0,
                                  y0);
        this.pointerDevice.motion(pointerResources,
                                  time1,
                                  x1,
                                  y1);
        //then
        verify(wlPointerResource0).enter(eq(serial0),
                                         eq(wlSurfaceResource0),
                                         eq(Fixed.create(5)),
                                         eq(Fixed.create(8)));

        verify(wlPointerResource0).motion(eq(time1),
                                          eq(Fixed.create(5)),
                                          eq(Fixed.create(8)));
    }

    /**
     * button is pressed, cursor moves from no surface into surface, button is released.
     */
    @Test
    public void testButtonNoGrabMotion() throws Exception {
        //given

        //time
        final int time0 = 112358;
        final int time1 = 112459;
        final int time2 = 112712;
        final int time3 = 113209;

        //pointer position 0
        final int x0 = 20;
        final int y0 = 30;
        final Point pointerPos0 = Point.create(x0,
                                               y0);

        //pointer position 1
        final int x1 = 500;
        final int y1 = 600;
        final Point pointerPos1 = Point.create(x1,
                                               y1);

        //mock surface 0
        final WlSurfaceResource wlSurfaceResource0 = mock(WlSurfaceResource.class);
        final Client client0 = mock(Client.class);
        when(wlSurfaceResource0.getClient()).thenReturn(client0);
        final WlSurface wlSurface0 = mock(WlSurface.class);
        when(wlSurfaceResource0.getImplementation()).thenReturn(wlSurface0);
        final Surface surface0 = mock(Surface.class);
        when(wlSurface0.getSurface()).thenReturn(surface0);
        final SurfaceView       surfaceView0  = mock(SurfaceView.class);
        Collection<SurfaceView> surfaceViews0 = new LinkedList<>();
        surfaceViews0.add(surfaceView0);
        when(surface0.getViews()).thenReturn(surfaceViews0);


        //mock surface 0 local coordinates
        final Point localPointerPosition0Start = Point.create(1,
                                                              1);
        when(surfaceView0.local(this.pointerDevice.getPosition())).thenReturn(localPointerPosition0Start);
        final Point localPointerPosition00 = Point.create(2,
                                                          3);
        when(surfaceView0.local(eq(pointerPos0))).thenReturn(localPointerPosition00);
        final Point localPointerPosition01 = Point.create(5,
                                                          8);
        when(surfaceView0.local(eq(pointerPos1))).thenReturn(localPointerPosition01);

        when(surfaceView0.getWlSurfaceResource()).thenReturn(wlSurfaceResource0);

        final Set<WlPointerResource> pointerResources = new HashSet<>();

        //mock pointer 0 resource
        final WlPointerResource wlPointerResource0 = mock(WlPointerResource.class);
        when(wlPointerResource0.getClient()).thenReturn(client0);
        pointerResources.add(wlPointerResource0);

        //mock display
        final int serial0 = 90879;
        final int serial1 = 90881;
        final int serial2 = 90882;
        when(this.display.nextSerial()).thenReturn(serial0,
                                                   serial1,
                                                   serial2);

        //button 0
        final int button0 = 1;

        //mock scene
        when(this.scene.pickSurfaceView(eq(Point.create(x0,
                                                        y0)))).thenReturn(Optional.empty());
        when(this.scene.pickSurfaceView(eq(Point.create(x1,
                                                        y1)))).thenReturn(Optional.of(surfaceView0));

        //when
        this.pointerDevice.motion(pointerResources,
                                  time0,
                                  x0,
                                  y0);
        this.pointerDevice.button(pointerResources,
                                  time1,
                                  button0,
                                  WlPointerButtonState.PRESSED);
        this.pointerDevice.motion(pointerResources,
                                  time2,
                                  x1,
                                  y1);
        this.pointerDevice.button(pointerResources,
                                  time3,
                                  button0,
                                  WlPointerButtonState.RELEASED);
        //then
        verify(wlPointerResource0).enter(eq(serial0),
                                         eq(wlSurfaceResource0),
                                         eq(Fixed.create(5)),
                                         eq(Fixed.create(8)));

        verify(wlPointerResource0).motion(eq(time2),
                                          eq(Fixed.create(5)),
                                          eq(Fixed.create(8)));

        verify(wlPointerResource0,
               never()).button(anyInt(),
                               anyInt(),
                               anyInt(),
                               anyInt());
    }

    /**
     * button is pressed, cursor is over surface, button is released
     *
     * @throws Exception
     */
    @Test
    public void testButton() throws Exception {
        //given

        //time
        final int time0 = 112358;
        final int time1 = 112459;
        final int time2 = 112712;

        //pointer position 0
        final int x0 = 20;
        final int y0 = 30;
        final Point pointerPos0 = Point.create(x0,
                                               y0);

        //pointer position 1
        final int x1 = 500;
        final int y1 = 600;
        final Point pointerPos1 = Point.create(x1,
                                               y1);

        //mock surface 0
        final WlSurfaceResource wlSurfaceResource0 = mock(WlSurfaceResource.class);
        final Client client0 = mock(Client.class);
        when(wlSurfaceResource0.getClient()).thenReturn(client0);
        final WlSurface wlSurface0 = mock(WlSurface.class);
        when(wlSurfaceResource0.getImplementation()).thenReturn(wlSurface0);
        final Surface surface0 = mock(Surface.class);
        when(wlSurface0.getSurface()).thenReturn(surface0);
        final SurfaceView       surfaceView0  = mock(SurfaceView.class);
        Collection<SurfaceView> surfaceViews0 = new LinkedList<>();
        surfaceViews0.add(surfaceView0);
        when(surface0.getViews()).thenReturn(surfaceViews0);

        //mock surface 0 local coordinates
        final Point localPointerPosition0Start = Point.create(1,
                                                              1);
        when(surfaceView0.local(this.pointerDevice.getPosition())).thenReturn(localPointerPosition0Start);
        final Point localPointerPosition00 = Point.create(2,
                                                          3);
        when(surfaceView0.local(eq(pointerPos0))).thenReturn(localPointerPosition00);
        final Point localPointerPosition01 = Point.create(5,
                                                          8);
        when(surfaceView0.local(eq(pointerPos1))).thenReturn(localPointerPosition01);

        when(surfaceView0.getWlSurfaceResource()).thenReturn(wlSurfaceResource0);

        final Set<WlPointerResource> pointerResources = new HashSet<>();

        //mock pointer 0 resource
        final WlPointerResource wlPointerResource0 = mock(WlPointerResource.class);
        when(wlPointerResource0.getClient()).thenReturn(client0);
        pointerResources.add(wlPointerResource0);

        //mock display
        final int serial0 = 90879;
        final int serial1 = 90881;
        final int serial2 = 90882;
        when(this.display.nextSerial()).thenReturn(serial0,
                                                   serial1,
                                                   serial2);

        //button 0
        final int button0 = 1;

        //mock scene
        when(this.scene.pickSurfaceView(eq(Point.create(x0,
                                                        y0)))).thenReturn(Optional.of(surfaceView0));
        when(this.scene.pickSurfaceView(eq(Point.create(x1,
                                                        y1)))).thenReturn(Optional.of(surfaceView0));

        //when
        this.pointerDevice.motion(pointerResources,
                                  time0,
                                  x0,
                                  y0);
        this.pointerDevice.button(pointerResources,
                                  time1,
                                  button0,
                                  WlPointerButtonState.PRESSED);
        this.pointerDevice.button(pointerResources,
                                  time2,
                                  button0,
                                  WlPointerButtonState.RELEASED);
        //then
        verify(wlPointerResource0).enter(eq(serial0),
                                         eq(wlSurfaceResource0),
                                         eq(Fixed.create(2)),
                                         eq(Fixed.create(3)));

        verify(wlPointerResource0).motion(eq(time0),
                                          eq(Fixed.create(2)),
                                          eq(Fixed.create(3)));

        verify(wlPointerResource0).button(serial1,
                                          time1,
                                          button0,
                                          WlPointerButtonState.PRESSED.value);

        verify(wlPointerResource0).button(serial2,
                                          time2,
                                          button0,
                                          WlPointerButtonState.RELEASED.value);
    }

    @Test
    public void testIsButtonPressed() throws Exception {
        //given

        //time
        final int time0 = 112358;
        final int time1 = 112459;

        //pointer position 0
        final int x0 = 20;
        final int y0 = 30;
        final Point pointerPos0 = Point.create(x0,
                                               y0);

        //pointer position 1
        final int x1 = 500;
        final int y1 = 600;
        final Point pointerPos1 = Point.create(x1,
                                               y1);

        //mock surface 0
        final WlSurfaceResource wlSurfaceResource0 = mock(WlSurfaceResource.class);
        final Client client0 = mock(Client.class);
        when(wlSurfaceResource0.getClient()).thenReturn(client0);
        final WlSurface wlSurface0 = mock(WlSurface.class);
        when(wlSurfaceResource0.getImplementation()).thenReturn(wlSurface0);
        final Surface surface0 = mock(Surface.class);
        when(wlSurface0.getSurface()).thenReturn(surface0);
        final SurfaceView       surfaceView0  = mock(SurfaceView.class);
        Collection<SurfaceView> surfaceViews0 = new LinkedList<>();
        surfaceViews0.add(surfaceView0);
        when(surface0.getViews()).thenReturn(surfaceViews0);

        //mock surface 0 local coordinates
        final Point localPointerPosition0Start = Point.create(1,
                                                              1);
        when(surfaceView0.local(this.pointerDevice.getPosition())).thenReturn(localPointerPosition0Start);
        final Point localPointerPosition00 = Point.create(2,
                                                          3);
        when(surfaceView0.local(eq(pointerPos0))).thenReturn(localPointerPosition00);
        final Point localPointerPosition01 = Point.create(5,
                                                          8);
        when(surfaceView0.local(eq(pointerPos1))).thenReturn(localPointerPosition01);

        when(surfaceView0.getWlSurfaceResource()).thenReturn(wlSurfaceResource0);

        final Set<WlPointerResource> pointerResources = new HashSet<>();

        //mock pointer 0 resource
        final WlPointerResource wlPointerResource0 = mock(WlPointerResource.class);
        when(wlPointerResource0.getClient()).thenReturn(client0);
        pointerResources.add(wlPointerResource0);

        //mock display
        final int serial0 = 90879;
        final int serial1 = 90881;
        final int serial2 = 90882;
        when(this.display.nextSerial()).thenReturn(serial0,
                                                   serial1,
                                                   serial2);

        //button 0
        final int button0 = 1;

        //mock scene
        when(this.scene.pickSurfaceView(eq(Point.create(x0,
                                                        y0)))).thenReturn(Optional.of(surfaceView0));

        //when
        this.pointerDevice.motion(pointerResources,
                                  time0,
                                  x0,
                                  y0);
        this.pointerDevice.button(pointerResources,
                                  time1,
                                  button0,
                                  WlPointerButtonState.PRESSED);
        //then
        assertThat(this.pointerDevice.isButtonPressed(button0)).isTrue();
    }


    @Test
    public void testCursorMoveOtherClientMotion() {
        // given: an active cursor for a surface, a second surface
        final SceneLayer sceneLayer = mock(SceneLayer.class);
        when(this.scene.getCursorLayer()).thenReturn(sceneLayer);

        final Client client0 = mock(Client.class);
        final Client client1 = mock(Client.class);

        final WlPointerResource wlPointerResource0 = mock(WlPointerResource.class);
        when(wlPointerResource0.getClient()).thenReturn(client0);
        final WlPointerResource wlPointerResource1 = mock(WlPointerResource.class);
        when(wlPointerResource1.getClient()).thenReturn(client1);
        final Set<WlPointerResource> wlPointerResources = new HashSet<>();
        wlPointerResources.add(wlPointerResource0);
        wlPointerResources.add(wlPointerResource1);

        final int x = 100;
        final int y = 200;

        final LinkedList<WlSurfaceResource> wlSurfaceResources = new LinkedList<>();

        final WlSurfaceResource wlSurfaceResource0 = mock(WlSurfaceResource.class);
        wlSurfaceResources.add(wlSurfaceResource0);

        when(wlSurfaceResource0.getClient()).thenReturn(client0);
        final WlSurface wlSurface0 = mock(WlSurface.class);
        when(wlSurfaceResource0.getImplementation()).thenReturn(wlSurface0);
        final Surface surface0 = mock(Surface.class);
        when(wlSurface0.getSurface()).thenReturn(surface0);
        final SurfaceView       surfaceView0  = mock(SurfaceView.class);
        Collection<SurfaceView> surfaceViews0 = new LinkedList<>();
        surfaceViews0.add(surfaceView0);
        when(surface0.getViews()).thenReturn(surfaceViews0);
        when(surfaceView0.getWlSurfaceResource()).thenReturn(wlSurfaceResource0);

        final Point localPointerPosition0 = mock(Point.class);
        when(surfaceView0.local(this.pointerDevice.getPosition())).thenReturn(localPointerPosition0);

        final WlSurfaceResource wlSurfaceResource1 = mock(WlSurfaceResource.class);
        wlSurfaceResources.add(wlSurfaceResource1);
        when(wlSurfaceResource1.getClient()).thenReturn(client1);
        final WlSurface wlSurface1 = mock(WlSurface.class);
        when(wlSurfaceResource1.getImplementation()).thenReturn(wlSurface1);
        final Surface surface1 = mock(Surface.class);
        when(wlSurface1.getSurface()).thenReturn(surface1);
        final SurfaceView       surfaceView1  = mock(SurfaceView.class);
        Collection<SurfaceView> surfaceViews1 = new LinkedList<>();
        surfaceViews1.add(surfaceView1);
        when(surface1.getViews()).thenReturn(surfaceViews1);
        when(surfaceView1.getWlSurfaceResource()).thenReturn(wlSurfaceResource1);


        final Point localPointerPosition1 = mock(Point.class);
        when(surfaceView1.local(Point.create(x,
                                             y))).thenReturn(localPointerPosition1);

        final WlSurfaceResource wlSurfaceResourceCursor0 = mock(WlSurfaceResource.class);
        when(wlSurfaceResourceCursor0.getClient()).thenReturn(client0);
        final WlSurface wlSurfaceCursor0 = mock(WlSurface.class);
        when(wlSurfaceResourceCursor0.getImplementation()).thenReturn(wlSurfaceCursor0);
        final Surface surfaceCursor0 = mock(Surface.class);
        when(wlSurfaceCursor0.getSurface()).thenReturn(surfaceCursor0);
        final SurfaceState surfaceStateCursor0 = SurfaceState.builder()
                                                             .build();
        when(surfaceCursor0.getState()).thenReturn(surfaceStateCursor0);
        final Cursor cursor0 = mock(Cursor.class);
        when(this.cursorFactory.create(eq(wlSurfaceResourceCursor0),
                                       any())).thenReturn(cursor0);
        when(cursor0.getWlSurfaceResource()).thenReturn(wlSurfaceResourceCursor0);
        this.pointerDevice.setCursor(wlPointerResource0,
                                     this.pointerDevice.getEnterSerial(),
                                     wlSurfaceResourceCursor0,
                                     0,
                                     0);

        //mock scene
        when(this.scene.pickSurfaceView(eq(Point.create(x,
                                                        y)))).thenReturn(Optional.of(surfaceView1));

        // when: cursor moves outside of old surface to new surface
        this.pointerDevice.motion(wlPointerResources,
                                  1234,
                                  x,
                                  y);

        final WlSurfaceResource wlSurfaceResourceCursor1 = mock(WlSurfaceResource.class);
        when(wlSurfaceResourceCursor1.getClient()).thenReturn(client1);
        final WlSurface wlSurfaceCursor1 = mock(WlSurface.class);
        when(wlSurfaceResourceCursor1.getImplementation()).thenReturn(wlSurfaceCursor1);
        final Surface surfaceCursor1 = mock(Surface.class);
        when(wlSurfaceCursor1.getSurface()).thenReturn(surfaceCursor1);
        final SurfaceState surfaceStateCursor1 = SurfaceState.builder()
                                                             .build();
        when(surfaceCursor1.getState()).thenReturn(surfaceStateCursor1);
        final Cursor cursor1 = mock(Cursor.class);
        when(this.cursorFactory.create(eq(wlSurfaceResourceCursor1),
                                       any())).thenReturn(cursor1);
        when(cursor1.getWlSurfaceResource()).thenReturn(wlSurfaceResourceCursor1);
        this.pointerDevice.setCursor(wlPointerResource1,
                                     this.pointerDevice.getEnterSerial(),
                                     wlSurfaceResourceCursor1,
                                     0,
                                     0);

        // then: old cursor is hidden and new cursor's position is updated.
        verify(cursor0).hide();
        verify(cursor1).updatePosition(Point.create(x,
                                                    y));
    }

    @Test
    public void testCursorMoveSameClientMotion() {
        //given: an active cursor
        final SceneLayer sceneLayer = mock(SceneLayer.class);
        when(this.scene.getCursorLayer()).thenReturn(sceneLayer);

        final Client client0 = mock(Client.class);

        final WlPointerResource wlPointerResource0 = mock(WlPointerResource.class);
        when(wlPointerResource0.getClient()).thenReturn(client0);
        final Set<WlPointerResource> wlPointerResources = new HashSet<>();
        wlPointerResources.add(wlPointerResource0);

        final int x = 100;
        final int y = 200;

        final LinkedList<WlSurfaceResource> wlSurfaceResources = new LinkedList<>();

        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
        wlSurfaceResources.add(wlSurfaceResource);
        when(wlSurfaceResource.getClient()).thenReturn(client0);
        final WlSurface wlSurface = mock(WlSurface.class);
        when(wlSurfaceResource.getImplementation()).thenReturn(wlSurface);
        final Surface surface = mock(Surface.class);
        when(wlSurface.getSurface()).thenReturn(surface);
        final SurfaceView       surfaceView0  = mock(SurfaceView.class);
        Collection<SurfaceView> surfaceViews0 = new LinkedList<>();
        surfaceViews0.add(surfaceView0);
        when(surface.getViews()).thenReturn(surfaceViews0);
        when(surfaceView0.getWlSurfaceResource()).thenReturn(wlSurfaceResource);

        final Point localPointerPosition0 = mock(Point.class);
        when(surfaceView0.local(this.pointerDevice.getPosition())).thenReturn(localPointerPosition0);

        final Point localPointerPosition1 = mock(Point.class);
        when(surfaceView0.local(Point.create(x,
                                             y))).thenReturn(localPointerPosition1);

        final WlSurfaceResource wlSurfaceResourceCursor0 = mock(WlSurfaceResource.class);
        when(wlSurfaceResourceCursor0.getClient()).thenReturn(client0);
        final WlSurface wlSurfaceCursor0 = mock(WlSurface.class);
        when(wlSurfaceResourceCursor0.getImplementation()).thenReturn(wlSurfaceCursor0);
        final Surface surfaceCursor0 = mock(Surface.class);
        when(wlSurfaceCursor0.getSurface()).thenReturn(surfaceCursor0);
        final SurfaceState surfaceStateCursor0 = SurfaceState.builder()
                                                             .build();
        when(surfaceCursor0.getState()).thenReturn(surfaceStateCursor0);
        final Cursor cursor0 = mock(Cursor.class);
        when(this.cursorFactory.create(eq(wlSurfaceResourceCursor0),
                                       any())).thenReturn(cursor0);
        when(cursor0.getWlSurfaceResource()).thenReturn(wlSurfaceResourceCursor0);

        //mock scene
        when(this.scene.pickSurfaceView(eq(Point.create(0,
                                                        0)))).thenReturn(Optional.empty());
        when(this.scene.pickSurfaceView(eq(Point.create(x,
                                                        y)))).thenReturn(Optional.of(surfaceView0));

        this.pointerDevice.motion(wlPointerResources,
                                  1234,
                                  0,
                                  0);

        this.pointerDevice.setCursor(wlPointerResource0,
                                     this.pointerDevice.getEnterSerial(),
                                     wlSurfaceResourceCursor0,
                                     0,
                                     0);


        //when: cursor moves inside of client area
        this.pointerDevice.motion(wlPointerResources,
                                  1235,
                                  x,
                                  y);

        //then: client cursor is not hidden and position is updated.
        verify(cursor0,
               never()).hide();
        verify(cursor0).updatePosition(Point.create(x,
                                                    y));
    }

    @Test
    public void testGrabMotion() throws Exception {
        //given

        //time
        final int time0 = 112358;
        final int time1 = 112459;
        final int time2 = 112712;
        final int time3 = 113209;
        final int time4 = 113319;

        //pointer position 0
        final int x0 = 20;
        final int y0 = 30;
        final Point pointerPos0 = Point.create(x0,
                                               y0);

        //pointer position 1
        final int x1 = 500;
        final int y1 = 600;
        final Point pointerPos1 = Point.create(x1,
                                               y1);

        //mock surface 0
        final WlSurfaceResource wlSurfaceResource0 = mock(WlSurfaceResource.class);
        final Client client0 = mock(Client.class);
        when(wlSurfaceResource0.getClient()).thenReturn(client0);
        final WlSurface wlSurface0 = mock(WlSurface.class);
        when(wlSurfaceResource0.getImplementation()).thenReturn(wlSurface0);
        final Surface surface0 = mock(Surface.class);
        when(wlSurface0.getSurface()).thenReturn(surface0);
        final SurfaceView       surfaceView0  = mock(SurfaceView.class);
        Collection<SurfaceView> surfaceViews0 = new LinkedList<>();
        surfaceViews0.add(surfaceView0);
        when(surface0.getViews()).thenReturn(surfaceViews0);

        //mock surface 0 local coordinates
        final Point localPointerPosition0Start = Point.create(1,
                                                              1);
        when(surfaceView0.local(this.pointerDevice.getPosition())).thenReturn(localPointerPosition0Start);
        final Point localPointerPosition00 = Point.create(2,
                                                          3);
        when(surfaceView0.local(eq(pointerPos0))).thenReturn(localPointerPosition00);
        final Point localPointerPosition01 = Point.create(5,
                                                          8);
        when(surfaceView0.local(eq(pointerPos1))).thenReturn(localPointerPosition01);
        when(surfaceView0.getWlSurfaceResource()).thenReturn(wlSurfaceResource0);

        //mock display
        final int serial0 = 90879;
        final int serial1 = 90881;
        final int serial2 = 90882;
        when(this.display.nextSerial()).thenReturn(serial0,
                                                   serial1,
                                                   serial2);

        final Set<WlPointerResource> pointerResources = new HashSet<>();

        //mock pointer 0 resource
        final WlPointerResource wlPointerResource0 = mock(WlPointerResource.class);
        when(wlPointerResource0.getClient()).thenReturn(client0);
        pointerResources.add(wlPointerResource0);

        //button 0
        final int button0 = 1;

        //button 1
        final int button1 = 3;

        //mock pointer grab motion
        final PointerGrabMotion pointerGrabMotion = mock(PointerGrabMotion.class);

        //mock scene
        when(this.scene.pickSurfaceView(eq(Point.create(x0,
                                                        y0)))).thenReturn(Optional.of(surfaceView0));
        when(this.scene.pickSurfaceView(eq(Point.create(x1,
                                                        y1)))).thenReturn(Optional.of(surfaceView0));

        //when
        this.pointerDevice.motion(pointerResources,
                                  time0,
                                  x0,
                                  y0);
        this.pointerDevice.button(pointerResources,
                                  time1,
                                  button0,
                                  WlPointerButtonState.PRESSED);
        this.pointerDevice.button(pointerResources,
                                  time2,
                                  button1,
                                  WlPointerButtonState.PRESSED);
        this.pointerDevice.grabMotion(wlSurfaceResource0,
                                      serial2,
                                      pointerGrabMotion);
        this.pointerDevice.motion(pointerResources,
                                  time3,
                                  x1,
                                  y1);

        //then
        verify(pointerGrabMotion).motion(eq(PointerMotion.create(time3,
                                                                 Point.create(x1,
                                                                              y1))));
        final ArgumentCaptor<DestroyListener> listenerArgumentCaptor = ArgumentCaptor.forClass(DestroyListener.class);
        verify(wlSurfaceResource0,
               times(3)).register(listenerArgumentCaptor.capture());
        //and when
        final List<DestroyListener> listeners = listenerArgumentCaptor.getAllValues();
        listeners.forEach(DestroyListener::handle);
        this.pointerDevice.motion(pointerResources,
                                  time4,
                                  x1,
                                  y1);
        //then
        verifyNoMoreInteractions(pointerGrabMotion);
    }

    @Test
    public void testRemoveCursor() throws Exception {
        // given: pointer with no surface
        final SceneLayer sceneLayer = mock(SceneLayer.class);
        when(this.scene.getCursorLayer()).thenReturn(sceneLayer);

        final Client            client            = mock(Client.class);
        final WlPointerResource wlPointerResource = mock(WlPointerResource.class);
        when(wlPointerResource.getClient()).thenReturn(client);

        final WlSurfaceResource wlSurfaceResourceCursor = mock(WlSurfaceResource.class);
        when(wlSurfaceResourceCursor.getClient()).thenReturn(client);
        final WlSurface wlSurfaceCursor = mock(WlSurface.class);
        when(wlSurfaceResourceCursor.getImplementation()).thenReturn(wlSurfaceCursor);
        final Surface surfaceCursor = mock(Surface.class);
        when(wlSurfaceCursor.getSurface()).thenReturn(surfaceCursor);
        final SurfaceState surfaceStateCursor = SurfaceState.builder()
                                                            .build();
        when(surfaceCursor.getState()).thenReturn(surfaceStateCursor);
        final Cursor cursor = mock(Cursor.class);
        when(this.cursorFactory.create(eq(wlSurfaceResourceCursor),
                                       any())).thenReturn(cursor);
        when(cursor.getWlSurfaceResource()).thenReturn(wlSurfaceResourceCursor);

        this.pointerDevice.setCursor(wlPointerResource,
                                     this.pointerDevice.getEnterSerial(),
                                     wlSurfaceResourceCursor,
                                     0,
                                     0);

        // when: remove cursor is called
        this.pointerDevice.removeCursor(wlPointerResource,
                                        this.pointerDevice.getEnterSerial());
        // then: cursor is hidden.
        verify(cursor).hide();
    }

    @Test
    public void testSetCursorUpdateHotspot() throws Exception {
        // given: pointer with surface
        final SceneLayer sceneLayer = mock(SceneLayer.class);
        when(this.scene.getCursorLayer()).thenReturn(sceneLayer);

        final Client            client            = mock(Client.class);
        final WlPointerResource wlPointerResource = mock(WlPointerResource.class);
        when(wlPointerResource.getClient()).thenReturn(client);

        final WlSurfaceResource wlSurfaceResourceCursor = mock(WlSurfaceResource.class);
        when(wlSurfaceResourceCursor.getClient()).thenReturn(client);
        final WlSurface wlSurfaceCursor = mock(WlSurface.class);
        when(wlSurfaceResourceCursor.getImplementation()).thenReturn(wlSurfaceCursor);
        final Surface surfaceCursor = mock(Surface.class);
        when(wlSurfaceCursor.getSurface()).thenReturn(surfaceCursor);
        final SurfaceState surfaceStateCursor = SurfaceState.builder()
                                                            .build();
        when(surfaceCursor.getState()).thenReturn(surfaceStateCursor);
        final Cursor cursor = mock(Cursor.class);
        when(this.cursorFactory.create(eq(wlSurfaceResourceCursor),
                                       any())).thenReturn(cursor);
        when(cursor.getWlSurfaceResource()).thenReturn(wlSurfaceResourceCursor);

        this.pointerDevice.setCursor(wlPointerResource,
                                     this.pointerDevice.getEnterSerial(),
                                     wlSurfaceResourceCursor,
                                     0,
                                     0);

        final int hotspotX = 11;
        final int hotspotY = 23;

        // when: surface is same as previous surface
        this.pointerDevice.setCursor(wlPointerResource,
                                     this.pointerDevice.getEnterSerial(),
                                     wlSurfaceResourceCursor,
                                     hotspotX,
                                     hotspotY);

        // then: cursor hotspot is updated and no additional destroy listener is registered for pointer
        verify(cursor).setHotspot(Point.create(hotspotX,
                                               hotspotY));
        final ArgumentCaptor<DestroyListener> destroyListenerArgumentCaptor = ArgumentCaptor.forClass(DestroyListener.class);
        verify(wlPointerResource,
               atMost(1)).register(destroyListenerArgumentCaptor.capture());

        // and when: pointer is destroyed
        destroyListenerArgumentCaptor.getValue()
                                     .handle();

        // then: cursor is made invisible
        verify(cursor).hide();
    }

    @Test
    public void testPreviousCursorNullSetCursor() throws Exception {
        // given: pointer with no surface
        final SceneLayer sceneLayer = mock(SceneLayer.class);
        when(this.scene.getCursorLayer()).thenReturn(sceneLayer);

        final Client            client            = mock(Client.class);
        final WlPointerResource wlPointerResource = mock(WlPointerResource.class);
        when(wlPointerResource.getClient()).thenReturn(client);

        final WlSurfaceResource wlSurfaceResourceCursor = mock(WlSurfaceResource.class);
        when(wlSurfaceResourceCursor.getClient()).thenReturn(client);
        final WlSurface wlSurfaceCursor = mock(WlSurface.class);
        when(wlSurfaceResourceCursor.getImplementation()).thenReturn(wlSurfaceCursor);
        final Surface surfaceCursor = mock(Surface.class);
        when(wlSurfaceCursor.getSurface()).thenReturn(surfaceCursor);
        final SurfaceState surfaceStateCursor = SurfaceState.builder()
                                                            .build();
        when(surfaceCursor.getState()).thenReturn(surfaceStateCursor);
        final Cursor cursor   = mock(Cursor.class);
        final int    hotspotX = 12;
        final int    hotspotY = 34;
        when(this.cursorFactory.create(eq(wlSurfaceResourceCursor),
                                       eq(Point.create(hotspotX,
                                                       hotspotY)))).thenReturn(cursor);
        when(cursor.getWlSurfaceResource()).thenReturn(wlSurfaceResourceCursor);

        // when: cursor surface is set
        this.pointerDevice.setCursor(wlPointerResource,
                                     this.pointerDevice.getEnterSerial(),
                                     wlSurfaceResourceCursor,
                                     hotspotX,
                                     hotspotY);

        // then: cursor is made visible and hotspot is updated and destroy listener is registered for pointer
        verify(cursor).updatePosition(this.pointerDevice.getPosition());

        final ArgumentCaptor<DestroyListener> destroyListenerArgumentCaptor = ArgumentCaptor.forClass(DestroyListener.class);
        verify(wlPointerResource).register(destroyListenerArgumentCaptor.capture());

        // and when: pointer is destroyed
        destroyListenerArgumentCaptor.getValue()
                                     .handle();

        // then: cursor is made invisible
        verify(cursor).hide();
    }

    @Test
    public void testSerialMismatchSetCursor() throws Exception {
        // given: pointer with surface with a wrong enter event serial
        final Client            client            = mock(Client.class);
        final WlPointerResource wlPointerResource = mock(WlPointerResource.class);
        when(wlPointerResource.getClient()).thenReturn(client);

        final WlSurfaceResource wlSurfaceResourceCursor = mock(WlSurfaceResource.class);
        when(wlSurfaceResourceCursor.getClient()).thenReturn(client);
        final WlSurface wlSurfaceCursor = mock(WlSurface.class);
        when(wlSurfaceResourceCursor.getImplementation()).thenReturn(wlSurfaceCursor);
        final Surface surfaceCursor = mock(Surface.class);
        when(wlSurfaceCursor.getSurface()).thenReturn(surfaceCursor);
        final SurfaceState surfaceStateCursor = SurfaceState.builder()
                                                            .build();
        when(surfaceCursor.getState()).thenReturn(surfaceStateCursor);
        final Cursor cursor   = mock(Cursor.class);
        final int    hotspotX = 12;
        final int    hotspotY = 34;
        when(this.cursorFactory.create(eq(wlSurfaceResourceCursor),
                                       eq(Point.create(hotspotX,
                                                       hotspotY)))).thenReturn(cursor);
        when(cursor.getWlSurfaceResource()).thenReturn(wlSurfaceResourceCursor);

        final int wrongSerial = 12356;

        // when: set cursor is called
        this.pointerDevice.setCursor(wlPointerResource,
                                     wrongSerial,
                                     wlSurfaceResourceCursor,
                                     hotspotX,
                                     hotspotY);

        // then: call is ignored
        verifyZeroInteractions(this.cursorFactory);
        verifyZeroInteractions(wlPointerResource);
        verifyZeroInteractions(wlSurfaceResourceCursor);
    }

    @Test
    public void testBeforeCommit() throws Exception {
        // given: a surface that is the current cursor that is visible
        final SceneLayer sceneLayer = mock(SceneLayer.class);
        when(this.scene.getCursorLayer()).thenReturn(sceneLayer);

        final Client client = mock(Client.class);

        final WlPointerResource wlPointerResource0 = mock(WlPointerResource.class);
        when(wlPointerResource0.getClient()).thenReturn(client);
        final Set<WlPointerResource> wlPointerResources = new HashSet<>();
        wlPointerResources.add(wlPointerResource0);

        final LinkedList<WlSurfaceResource> wlSurfaceResources = new LinkedList<>();

        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
        wlSurfaceResources.add(wlSurfaceResource);
        when(wlSurfaceResource.getClient()).thenReturn(client);
        final WlSurface wlSurface = mock(WlSurface.class);
        when(wlSurfaceResource.getImplementation()).thenReturn(wlSurface);
        final Surface surface = mock(Surface.class);
        when(wlSurface.getSurface()).thenReturn(surface);
        final SurfaceView       surfaceView0  = mock(SurfaceView.class);
        Collection<SurfaceView> surfaceViews0 = new LinkedList<>();
        surfaceViews0.add(surfaceView0);
        when(surface.getViews()).thenReturn(surfaceViews0);
        when(surfaceView0.getWlSurfaceResource()).thenReturn(wlSurfaceResource);

        final Point localPointerPosition0 = mock(Point.class);
        when(surfaceView0.local(this.pointerDevice.getPosition())).thenReturn(localPointerPosition0);

        final WlSurfaceResource wlSurfaceResourceCursor = mock(WlSurfaceResource.class);
        when(wlSurfaceResourceCursor.getClient()).thenReturn(client);
        final WlSurface wlSurfaceCursor = mock(WlSurface.class);
        when(wlSurfaceResourceCursor.getImplementation()).thenReturn(wlSurfaceCursor);
        final Surface surfaceCursor = mock(Surface.class);
        when(wlSurfaceCursor.getSurface()).thenReturn(surfaceCursor);
        final WlBufferResource     wlBufferResourceCursor    = mock(WlBufferResource.class);
        final Region               inputRegion               = mock(Region.class);
        final int                  scale                     = 2;
        final SurfaceState.Builder surfaceStateCursorBuilder = mock(SurfaceState.Builder.class);
        final SurfaceState surfaceState = SurfaceState.builder()
                                                      .scale(scale)
                                                      .buffer(Optional.of(wlBufferResourceCursor))
                                                      .inputRegion(Optional.of(inputRegion))
                                                      .build();

        when(surfaceCursor.getState()).thenReturn(surfaceState);
        when(surfaceCursor.getPendingState()).thenReturn(surfaceStateCursorBuilder);
        final Cursor cursor = mock(Cursor.class);
        when(this.cursorFactory.create(eq(wlSurfaceResourceCursor),
                                       any())).thenReturn(cursor);
        when(cursor.getWlSurfaceResource()).thenReturn(wlSurfaceResourceCursor);

        //mock scene
        when(this.scene.pickSurfaceView(eq(Point.create(0,
                                                        0)))).thenReturn(Optional.of(surfaceView0));

        this.pointerDevice.motion(wlPointerResources,
                                  1234,
                                  0,
                                  0);

        this.pointerDevice.setCursor(wlPointerResource0,
                                     this.pointerDevice.getEnterSerial(),
                                     wlSurfaceResourceCursor,
                                     0,
                                     0);

        // when: the surface state is about to be committed,
        this.pointerDevice.beforeCommit(wlSurfaceResourceCursor);

        // then: the cursor surface's input region is set to the null region
        verify(surfaceStateCursorBuilder).inputRegion(Optional.of(this.nullRegion));
    }

    @Test
    public void testNotCurrentCursorBeforeCommit() throws Exception {
        // given: a surface that is not the current cursor
        final SceneLayer sceneLayer = mock(SceneLayer.class);
        when(this.scene.getCursorLayer()).thenReturn(sceneLayer);

        final Client client0 = mock(Client.class);
        final Client client1 = mock(Client.class);

        final WlPointerResource wlPointerResource0 = mock(WlPointerResource.class);
        when(wlPointerResource0.getClient()).thenReturn(client0);
        final WlPointerResource wlPointerResource1 = mock(WlPointerResource.class);
        when(wlPointerResource1.getClient()).thenReturn(client1);
        final Set<WlPointerResource> wlPointerResources = new HashSet<>();
        wlPointerResources.add(wlPointerResource0);
        wlPointerResources.add(wlPointerResource1);

        final int x = 100;
        final int y = 200;

        final LinkedList<WlSurfaceResource> wlSurfaceResources = new LinkedList<>();

        final WlSurfaceResource wlSurfaceResource0 = mock(WlSurfaceResource.class);
        wlSurfaceResources.add(wlSurfaceResource0);

        when(wlSurfaceResource0.getClient()).thenReturn(client0);
        final WlSurface wlSurface0 = mock(WlSurface.class);
        when(wlSurfaceResource0.getImplementation()).thenReturn(wlSurface0);
        final Surface surface0 = mock(Surface.class);
        when(wlSurface0.getSurface()).thenReturn(surface0);
        final SurfaceView       surfaceView0  = mock(SurfaceView.class);
        Collection<SurfaceView> surfaceViews0 = new LinkedList<>();
        surfaceViews0.add(surfaceView0);
        when(surface0.getViews()).thenReturn(surfaceViews0);
        when(surfaceView0.getWlSurfaceResource()).thenReturn(wlSurfaceResource0);

        final Point localPointerPosition0 = mock(Point.class);
        when(surfaceView0.local(this.pointerDevice.getPosition())).thenReturn(localPointerPosition0);

        final WlSurfaceResource wlSurfaceResource1 = mock(WlSurfaceResource.class);
        wlSurfaceResources.add(wlSurfaceResource1);
        when(wlSurfaceResource1.getClient()).thenReturn(client1);
        final WlSurface wlSurface1 = mock(WlSurface.class);
        when(wlSurfaceResource1.getImplementation()).thenReturn(wlSurface1);
        final Surface surface1 = mock(Surface.class);
        when(wlSurface1.getSurface()).thenReturn(surface1);
        final SurfaceView       surfaceView1  = mock(SurfaceView.class);
        Collection<SurfaceView> surfaceViews1 = new LinkedList<>();
        surfaceViews1.add(surfaceView1);
        when(surface1.getViews()).thenReturn(surfaceViews1);
        when(surfaceView1.getWlSurfaceResource()).thenReturn(wlSurfaceResource1);

        final Point localPointerPosition1 = mock(Point.class);
        when(surfaceView1.local(Point.create(x,
                                             y))).thenReturn(localPointerPosition1);

        final WlSurfaceResource wlSurfaceResourceCursor0 = mock(WlSurfaceResource.class);
        when(wlSurfaceResourceCursor0.getClient()).thenReturn(client0);
        final WlSurface wlSurfaceCursor0 = mock(WlSurface.class);
        when(wlSurfaceResourceCursor0.getImplementation()).thenReturn(wlSurfaceCursor0);
        final Surface surfaceCursor0 = mock(Surface.class);
        when(wlSurfaceCursor0.getSurface()).thenReturn(surfaceCursor0);
        final WlBufferResource wlBufferResourceCursor0 = mock(WlBufferResource.class);
        final Region           inputRegion0            = mock(Region.class);
        final int              scale0                  = 2;
        final SurfaceState surfaceStateCursor0 = SurfaceState.builder()
                                                             .scale(scale0)
                                                             .buffer(Optional.of(wlBufferResourceCursor0))
                                                             .inputRegion(Optional.of(inputRegion0))
                                                             .build();
        when(surfaceCursor0.getState()).thenReturn(surfaceStateCursor0);
        final SurfaceState.Builder stateBuilder = mock(SurfaceState.Builder.class);
        when(surfaceCursor0.getPendingState()).thenReturn(stateBuilder);
        final Cursor cursor0 = mock(Cursor.class);
        when(this.cursorFactory.create(eq(wlSurfaceResourceCursor0),
                                       any())).thenReturn(cursor0);
        when(cursor0.getWlSurfaceResource()).thenReturn(wlSurfaceResourceCursor0);

        final WlSurfaceResource wlSurfaceResourceCursor1 = mock(WlSurfaceResource.class);
        when(wlSurfaceResourceCursor1.getClient()).thenReturn(client1);
        final WlSurface wlSurfaceCursor1 = mock(WlSurface.class);
        when(wlSurfaceResourceCursor1.getImplementation()).thenReturn(wlSurfaceCursor1);
        final Surface surfaceCursor1 = mock(Surface.class);
        when(wlSurfaceCursor1.getSurface()).thenReturn(surfaceCursor1);
        final WlBufferResource wlBufferResourceCursor1 = mock(WlBufferResource.class);
        final Region           inputRegion1            = mock(Region.class);
        final int              scale1                  = 3;
        final SurfaceState.Builder surfaceStateCursor1 = SurfaceState.builder()
                                                                     .scale(scale1)
                                                                     .buffer(Optional.of(wlBufferResourceCursor1))
                                                                     .inputRegion(Optional.of(inputRegion1));

        when(surfaceCursor1.getState()).thenReturn(surfaceStateCursor1.build());
        final SurfaceState.Builder stateBuilder1 = mock(SurfaceState.Builder.class);
        when(surfaceCursor1.getPendingState()).thenReturn(stateBuilder1);
        final Cursor cursor1 = mock(Cursor.class);
        when(this.cursorFactory.create(eq(wlSurfaceResourceCursor1),
                                       any())).thenReturn(cursor1);
        when(cursor1.getWlSurfaceResource()).thenReturn(wlSurfaceResourceCursor1);

        //mock scene
        when(this.scene.pickSurfaceView(eq(Point.create(x,
                                                        y)))).thenReturn(Optional.of(surfaceView1));
        when(this.scene.pickSurfaceView(eq(Point.create(0,
                                                        0)))).thenReturn(Optional.of(surfaceView0));

        //move cursor from one client's surface to another
        //so 2 different cursor 'images' were set for 2 different clients
        this.pointerDevice.setCursor(wlPointerResource0,
                                     this.pointerDevice.getEnterSerial(),
                                     wlSurfaceResourceCursor0,
                                     0,
                                     0);
        this.pointerDevice.motion(wlPointerResources,
                                  1,
                                  x,
                                  y);
        this.pointerDevice.setCursor(wlPointerResource1,
                                     this.pointerDevice.getEnterSerial(),
                                     wlSurfaceResourceCursor1,
                                     0,
                                     0);
        this.pointerDevice.motion(wlPointerResources,
                                  2,
                                  0,
                                  0);
        this.pointerDevice.setCursor(wlPointerResource0,
                                     this.pointerDevice.getEnterSerial(),
                                     wlSurfaceResourceCursor0,
                                     0,
                                     0);

        // when: the surface state is about to be committed,
        this.pointerDevice.beforeCommit(wlSurfaceResourceCursor1);

        // then: the surface's input region and buffer is cleared.
        verify(stateBuilder1).buffer(Optional.empty());
        verify(stateBuilder1).inputRegion(Optional.of(this.nullRegion));
    }

    @Test
    public void testCurrentCursorNotVisibleBeforeCommit() throws Exception {
        // given: a surface that is the current cursor that is not visible
        final SceneLayer sceneLayer = mock(SceneLayer.class);
        when(this.scene.getCursorLayer()).thenReturn(sceneLayer);

        final Client client = mock(Client.class);

        final WlPointerResource wlPointerResource0 = mock(WlPointerResource.class);
        when(wlPointerResource0.getClient()).thenReturn(client);
        final Set<WlPointerResource> wlPointerResources = new HashSet<>();
        wlPointerResources.add(wlPointerResource0);

        final LinkedList<WlSurfaceResource> wlSurfaceResources = new LinkedList<>();

        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
        wlSurfaceResources.add(wlSurfaceResource);
        when(wlSurfaceResource.getClient()).thenReturn(client);
        final WlSurface wlSurface = mock(WlSurface.class);
        when(wlSurfaceResource.getImplementation()).thenReturn(wlSurface);
        final Surface surface = mock(Surface.class);
        when(wlSurface.getSurface()).thenReturn(surface);
        final SurfaceView       surfaceView0  = mock(SurfaceView.class);
        Collection<SurfaceView> surfaceViews0 = new LinkedList<>();
        surfaceViews0.add(surfaceView0);
        when(surface.getViews()).thenReturn(surfaceViews0);
        when(surfaceView0.getWlSurfaceResource()).thenReturn(wlSurfaceResource);

        final Point localPointerPosition0 = mock(Point.class);
        when(surfaceView0.local(this.pointerDevice.getPosition())).thenReturn(localPointerPosition0);

        final WlSurfaceResource wlSurfaceResourceCursor = mock(WlSurfaceResource.class);
        when(wlSurfaceResourceCursor.getClient()).thenReturn(client);
        final WlSurface wlSurfaceCursor = mock(WlSurface.class);
        when(wlSurfaceResourceCursor.getImplementation()).thenReturn(wlSurfaceCursor);
        final Surface surfaceCursor = mock(Surface.class);
        when(wlSurfaceCursor.getSurface()).thenReturn(surfaceCursor);
        final WlBufferResource wlBufferResourceCursor = mock(WlBufferResource.class);
        final Region           inputRegion            = mock(Region.class);
        final int              scale                  = 2;
        final SurfaceState surfaceStateCursor = SurfaceState.builder()
                                                            .scale(scale)
                                                            .buffer(Optional.of(wlBufferResourceCursor))
                                                            .inputRegion(Optional.of(inputRegion))
                                                            .build();
        when(surfaceCursor.getState()).thenReturn(surfaceStateCursor);
        final SurfaceState.Builder stateBuilder = mock(SurfaceState.Builder.class);
        when(surfaceCursor.getPendingState()).thenReturn(stateBuilder);
        final Cursor cursor = mock(Cursor.class);
        when(this.cursorFactory.create(eq(wlSurfaceResourceCursor),
                                       any())).thenReturn(cursor);
        when(cursor.getWlSurfaceResource()).thenReturn(wlSurfaceResourceCursor);
        when(cursor.isHidden()).thenReturn(true);

        //mock scene
        when(this.scene.pickSurfaceView(eq(Point.create(0,
                                                        0)))).thenReturn(Optional.of(surfaceView0));

        this.pointerDevice.motion(wlPointerResources,
                                  1,
                                  0,
                                  0);

        this.pointerDevice.setCursor(wlPointerResource0,
                                     this.pointerDevice.getEnterSerial(),
                                     wlSurfaceResourceCursor,
                                     0,
                                     0);

        // when: the surface state is about to be committed,
        this.pointerDevice.beforeCommit(wlSurfaceResourceCursor);

        // then: the surface's input region and buffer is cleared.
        verify(stateBuilder).buffer(Optional.empty());
        verify(stateBuilder).inputRegion(Optional.of(this.nullRegion));
    }

    @Test
    public void testAfterDestroy() throws Exception {
        // given: a cursor surface
        final SceneLayer sceneLayer = mock(SceneLayer.class);
        when(this.scene.getCursorLayer()).thenReturn(sceneLayer);

        final Client            client            = mock(Client.class);
        final WlPointerResource wlPointerResource = mock(WlPointerResource.class);
        when(wlPointerResource.getClient()).thenReturn(client);

        final WlSurfaceResource wlSurfaceResourceCursor = mock(WlSurfaceResource.class);
        when(wlSurfaceResourceCursor.getClient()).thenReturn(client);
        final WlSurface wlSurfaceCursor = mock(WlSurface.class);
        when(wlSurfaceResourceCursor.getImplementation()).thenReturn(wlSurfaceCursor);
        final Surface surfaceCursor = mock(Surface.class);
        when(wlSurfaceCursor.getSurface()).thenReturn(surfaceCursor);
        final SurfaceState surfaceStateCursor = SurfaceState.builder()
                                                            .build();
        when(surfaceCursor.getState()).thenReturn(surfaceStateCursor);
        final Cursor cursor   = mock(Cursor.class);
        final int    hotspotX = 12;
        final int    hotspotY = 34;
        when(this.cursorFactory.create(eq(wlSurfaceResourceCursor),
                                       eq(Point.create(hotspotX,
                                                       hotspotY)))).thenReturn(cursor);
        when(cursor.getWlSurfaceResource()).thenReturn(wlSurfaceResourceCursor);

        this.pointerDevice.setCursor(wlPointerResource,
                                     this.pointerDevice.getEnterSerial(),
                                     wlSurfaceResourceCursor,
                                     hotspotX,
                                     hotspotY);

        // when: cursor surface is destroyed
        this.pointerDevice.afterDestroy(wlSurfaceResourceCursor);

        // then: corresponding cursor is no longer tracked
        verify(cursor).hide();
    }

    @Test
    public void testGrabSurfaceDestroyed() throws Exception {
        // given: a surface that has the grab

        //mock surface 0
        final WlSurfaceResource wlSurfaceResource0 = mock(WlSurfaceResource.class);
        final Client client0 = mock(Client.class);
        when(wlSurfaceResource0.getClient()).thenReturn(client0);
        final WlSurface wlSurface0 = mock(WlSurface.class);
        when(wlSurfaceResource0.getImplementation()).thenReturn(wlSurface0);
        final Surface surface0 = mock(Surface.class);
        when(wlSurface0.getSurface()).thenReturn(surface0);
        final SurfaceView       surfaceView0  = mock(SurfaceView.class);
        Collection<SurfaceView> surfaceViews0 = new LinkedList<>();
        surfaceViews0.add(surfaceView0);
        when(surface0.getViews()).thenReturn(surfaceViews0);
        when(surfaceView0.getWlSurfaceResource()).thenReturn(wlSurfaceResource0);

        //mock surface 0 local coordinates
        final Point localPointerPosition0Start = mock(Point.class);
        when(surfaceView0.local(this.pointerDevice.getPosition())).thenReturn(localPointerPosition0Start);

        //mock pointer 0 resource
        final Set<WlPointerResource> pointerResources   = new HashSet<>();
        final WlPointerResource      wlPointerResource0 = mock(WlPointerResource.class);
        when(wlPointerResource0.getClient()).thenReturn(client0);
        pointerResources.add(wlPointerResource0);

        //mock scene
        when(this.scene.pickSurfaceView(eq(Point.create(0,
                                                        0)))).thenReturn(Optional.of(surfaceView0));

        this.pointerDevice.motion(pointerResources,
                                  1,
                                  0,
                                  0);

        this.pointerDevice.button(pointerResources,
                                  2,
                                  1,
                                  WlPointerButtonState.PRESSED);

        // when: the surface is destroyed
        final ArgumentCaptor<DestroyListener> destroyListenerArgumentCaptor = ArgumentCaptor.forClass(DestroyListener.class);
        verify(wlSurfaceResource0,
               atLeastOnce()).register(destroyListenerArgumentCaptor.capture());
        destroyListenerArgumentCaptor.getAllValues()
                                     .forEach(DestroyListener::handle);

        // then: the grab surface is forgotten
        assertThat(this.pointerDevice.getGrab()
                                     .isPresent()).isFalse();
    }

    @Test
    public void testFocusSurfaceDestroyedNewFocus() throws Exception {
        // given: a surface that has the focus, an underlying surface
        //mock jobexecutor
        doAnswer(invocation -> {
            Runnable runnable = (Runnable) invocation.getArguments()[0];
            runnable.run();
            return null;
        }).when(this.jobExecutor)
          .submit(any());

        final Set<WlPointerResource> pointerResources = new HashSet<>();

        //mock surface 0
        final WlSurfaceResource wlSurfaceResource0 = mock(WlSurfaceResource.class);
        final Client client0 = mock(Client.class);
        when(wlSurfaceResource0.getClient()).thenReturn(client0);
        final WlSurface wlSurface0 = mock(WlSurface.class);
        when(wlSurfaceResource0.getImplementation()).thenReturn(wlSurface0);
        final Surface surface0 = mock(Surface.class);
        when(wlSurface0.getSurface()).thenReturn(surface0);
        final SurfaceView       surfaceView0  = mock(SurfaceView.class);
        Collection<SurfaceView> surfaceViews0 = new LinkedList<>();
        surfaceViews0.add(surfaceView0);
        when(surface0.getViews()).thenReturn(surfaceViews0);
        when(surfaceView0.getWlSurfaceResource()).thenReturn(wlSurfaceResource0);

        //mock surface 0 local coordinates
        final Point localPointerPosition0Start = mock(Point.class);
        when(surfaceView0.local(this.pointerDevice.getPosition())).thenReturn(localPointerPosition0Start);

        //mock pointer 0 resource
        final WlPointerResource wlPointerResource0 = mock(WlPointerResource.class);
        when(wlPointerResource0.getClient()).thenReturn(client0);
        pointerResources.add(wlPointerResource0);

        //mock surface 1
        final WlSurfaceResource wlSurfaceResource1 = mock(WlSurfaceResource.class);
        final Client client1 = mock(Client.class);
        when(wlSurfaceResource1.getClient()).thenReturn(client1);
        final WlSurface wlSurface1 = mock(WlSurface.class);
        when(wlSurfaceResource1.getImplementation()).thenReturn(wlSurface1);
        final Surface surface1 = mock(Surface.class);
        when(wlSurface1.getSurface()).thenReturn(surface1);
        final SurfaceView       surfaceView1  = mock(SurfaceView.class);
        Collection<SurfaceView> surfaceViews1 = new LinkedList<>();
        surfaceViews1.add(surfaceView1);
        when(surface1.getViews()).thenReturn(surfaceViews1);
        when(surfaceView1.getWlSurfaceResource()).thenReturn(wlSurfaceResource1);

        //mock surface 1 local coordinates
        final Point localPointerPosition1Start = mock(Point.class);
        when(surfaceView1.local(this.pointerDevice.getPosition())).thenReturn(localPointerPosition1Start);

        //mock pointer 1 resource
        final WlPointerResource wlPointerResource1 = mock(WlPointerResource.class);
        when(wlPointerResource1.getClient()).thenReturn(client1);
        pointerResources.add(wlPointerResource1);

        //mock scene
        when(this.scene.pickSurfaceView(eq(Point.create(0,
                                                        0)))).thenReturn(Optional.of(surfaceView1),
                                                                         Optional.of(surfaceView0));

        this.pointerDevice.motion(pointerResources,
                                  1,
                                  0,
                                  0);

        // when: the surface is destroyed
        final ArgumentCaptor<DestroyListener> destroyListenerArgumentCaptor = ArgumentCaptor.forClass(DestroyListener.class);
        verify(wlSurfaceResource1,
               atLeastOnce()).register(destroyListenerArgumentCaptor.capture());
        destroyListenerArgumentCaptor.getAllValues()
                                     .forEach(DestroyListener::handle);

        // then: the underlying surface gets the focus
        assertThat(this.pointerDevice.getFocus()
                                     .get()).isEqualTo(surfaceView0);
    }

    @Test
    public void testFocusSurfaceDestroyedNoFocus() throws Exception {
        // given: a surface that has the focus
        //mock jobexecutor
        doAnswer(invocation -> {
            Runnable runnable = (Runnable) invocation.getArguments()[0];
            runnable.run();
            return null;
        }).when(this.jobExecutor)
          .submit(any());

        //mock surface 0
        final WlSurfaceResource wlSurfaceResource0 = mock(WlSurfaceResource.class);
        final Client client0 = mock(Client.class);
        when(wlSurfaceResource0.getClient()).thenReturn(client0);
        final WlSurface wlSurface0 = mock(WlSurface.class);
        when(wlSurfaceResource0.getImplementation()).thenReturn(wlSurface0);
        final Surface surface0 = mock(Surface.class);
        when(wlSurface0.getSurface()).thenReturn(surface0);
        final SurfaceView       surfaceView0  = mock(SurfaceView.class);
        Collection<SurfaceView> surfaceViews0 = new LinkedList<>();
        surfaceViews0.add(surfaceView0);
        when(surface0.getViews()).thenReturn(surfaceViews0);
        when(surfaceView0.getWlSurfaceResource()).thenReturn(wlSurfaceResource0);

        //mock surface 0 local coordinates
        final Point localPointerPosition0Start = mock(Point.class);
        when(surfaceView0.local(this.pointerDevice.getPosition())).thenReturn(localPointerPosition0Start);

        //mock pointer 0 resource
        final Set<WlPointerResource> pointerResources   = new HashSet<>();
        final WlPointerResource      wlPointerResource0 = mock(WlPointerResource.class);
        when(wlPointerResource0.getClient()).thenReturn(client0);
        pointerResources.add(wlPointerResource0);

        //mock scene
        when(this.scene.pickSurfaceView(eq(Point.create(0,
                                                        0)))).thenReturn(Optional.of(surfaceView0),
                                                                         Optional.empty());

        this.pointerDevice.motion(pointerResources,
                                  1,
                                  0,
                                  0);

        // when: the surface is destroyed
        final ArgumentCaptor<DestroyListener> destroyListenerArgumentCaptor = ArgumentCaptor.forClass(DestroyListener.class);
        verify(wlSurfaceResource0,
               atLeastOnce()).register(destroyListenerArgumentCaptor.capture());
        destroyListenerArgumentCaptor.getAllValues()
                                     .forEach(DestroyListener::handle);
        // then: no surface has the focus
        assertThat(this.pointerDevice.getFocus()
                                     .isPresent()).isFalse();
    }
}