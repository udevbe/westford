package org.westmalle.wayland.output;

import org.freedesktop.wayland.server.*;
import org.freedesktop.wayland.server.jna.WaylandServerLibrary;
import org.freedesktop.wayland.server.jna.WaylandServerLibraryMapping;
import org.freedesktop.wayland.shared.WlPointerButtonState;
import org.freedesktop.wayland.util.Fixed;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.westmalle.wayland.output.events.Motion;
import org.westmalle.wayland.protocol.WlRegion;
import org.westmalle.wayland.protocol.WlSurface;

import java.util.*;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.verifyNoMoreInteractions;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(WaylandServerLibrary.class)
public class PointerDeviceTest {

    @Mock
    private Display       display;
    @Mock
    private Compositor    compositor;
    @InjectMocks
    private PointerDevice pointerDevice;

    @Before
    public void setUp(){
        PowerMockito.mockStatic(WaylandServerLibrary.class);
        Mockito.when(WaylandServerLibrary.INSTANCE()).thenReturn(mock(WaylandServerLibraryMapping.class));
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
        final LinkedList<WlSurfaceResource> wlSurfaceResources = new LinkedList<>();
        when(this.compositor.getSurfacesStack()).thenReturn(wlSurfaceResources);

        //mock surface 0
        final WlSurfaceResource wlSurfaceResource0 = mock(WlSurfaceResource.class);
        wlSurfaceResources.add(wlSurfaceResource0);
        final Client client0 = mock(Client.class);
        when(wlSurfaceResource0.getClient()).thenReturn(client0);
        final WlSurface wlSurface0 = mock(WlSurface.class);
        when(wlSurfaceResource0.getImplementation()).thenReturn(wlSurface0);
        final Surface surface0 = mock(Surface.class);
        when(wlSurface0.getSurface()).thenReturn(surface0);
        final Rectangle size0 = mock(Rectangle.class);
        when(surface0.getSize()).thenReturn(size0);
        final SurfaceState surfaceState0 = mock(SurfaceState.class);
        when(surface0.getState()).thenReturn(surfaceState0);
        final WlRegionResource wlRegionResource0 = mock(WlRegionResource.class);
        when(surfaceState0.getInputRegion()).thenReturn(Optional.of(wlRegionResource0));
        final WlRegion wlRegion0 = mock(WlRegion.class);
        when(wlRegionResource0.getImplementation()).thenReturn(wlRegion0);
        final Region region0 = mock(Region.class);
        when(wlRegion0.getRegion()).thenReturn(region0);

        //mock surface 0 local coordinates
        final Point localPointerPosition0Start = Point.create(1,1);
        when(surface0.local(this.pointerDevice.getPosition())).thenReturn(localPointerPosition0Start);
        when(region0.contains(size0,
                              localPointerPosition0Start)).thenReturn(true);
        final Point localPointerPosition00 = Point.create(2,3);
        when(surface0.local(eq(pointerPos0))).thenReturn(localPointerPosition00);
        when(region0.contains(eq(size0),
                              eq(localPointerPosition00))).thenReturn(true);
        final Point localPointerPosition01 = Point.create(5,8);
        when(surface0.local(eq(pointerPos1))).thenReturn(localPointerPosition01);
        when(region0.contains(eq(size0),
                              eq(localPointerPosition01))).thenReturn(false);

        //mock surface 1
        final WlSurfaceResource wlSurfaceResource1 = mock(WlSurfaceResource.class);
        wlSurfaceResources.add(wlSurfaceResource1);
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
        final WlRegionResource wlRegionResource1 = mock(WlRegionResource.class);
        when(surfaceState1.getInputRegion()).thenReturn(Optional.of(wlRegionResource1));
        final WlRegion wlRegion1 = mock(WlRegion.class);
        when(wlRegionResource1.getImplementation()).thenReturn(wlRegion1);
        final Region region1 = mock(Region.class);
        when(wlRegion1.getRegion()).thenReturn(region1);

        //mock surface 1 local coordinates
        final Point localPointerPosition1Start = Point.create(13,21);
        when(surface1.local(this.pointerDevice.getPosition())).thenReturn(localPointerPosition1Start);
        when(region1.contains(size1,
                              localPointerPosition1Start)).thenReturn(false);
        final Point localPointerPosition10 = Point.create(34, 55);
        when(surface1.local(eq(pointerPos1))).thenReturn(localPointerPosition10);
        when(region1.contains(eq(size1),
                              eq(localPointerPosition10))).thenReturn(false);
        final Point localPointerPosition11 = Point.create(89, 144);
        when(surface1.local(eq(pointerPos1))).thenReturn(localPointerPosition11);
        when(region1.contains(eq(size1),
                              eq(localPointerPosition11))).thenReturn(true);

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
        //bug in wayland java bindings, we have to use an argument captor to compare Fixed object equality.
        final ArgumentCaptor<Fixed> fixedArgumentCaptor = ArgumentCaptor.forClass(Fixed.class);
        final List<Fixed> values = fixedArgumentCaptor.getAllValues();

        verify(wlPointerResource0).enter(eq(this.display.nextSerial()),
                                         eq(wlSurfaceResource0),
                                         fixedArgumentCaptor.capture(),
                                         fixedArgumentCaptor.capture());

        assertThat(values.get(0)
                         .asInt()).isEqualTo(2);
        assertThat(values.get(1)
                         .asInt()).isEqualTo(3);

        verify(wlPointerResource0).motion(eq(time0),
                                          fixedArgumentCaptor.capture(),
                                          fixedArgumentCaptor.capture());
        verify(wlPointerResource0).motion(eq(time2),
                                          fixedArgumentCaptor.capture(),
                                          fixedArgumentCaptor.capture());
        assertThat(values.get(2)
                         .asInt()).isEqualTo(2);
        assertThat(values.get(3)
                         .asInt()).isEqualTo(3);
        assertThat(values.get(4)
                         .asInt()).isEqualTo(5);
        assertThat(values.get(5)
                         .asInt()).isEqualTo(8);

        verify(wlPointerResource0).button(serial,
                                time1,
                                button0,
                                WlPointerButtonState.PRESSED.getValue());

        verify(wlPointerResource0).leave(this.display.nextSerial(),
                               wlSurfaceResource0);

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
        final LinkedList<WlSurfaceResource> wlSurfaceResources = new LinkedList<>();
        when(this.compositor.getSurfacesStack()).thenReturn(wlSurfaceResources);

        //mock surface 0
        final WlSurfaceResource wlSurfaceResource0 = mock(WlSurfaceResource.class);
        wlSurfaceResources.add(wlSurfaceResource0);
        final Client client0 = mock(Client.class);
        when(wlSurfaceResource0.getClient()).thenReturn(client0);
        final WlSurface wlSurface0 = mock(WlSurface.class);
        when(wlSurfaceResource0.getImplementation()).thenReturn(wlSurface0);
        final Surface surface0 = mock(Surface.class);
        when(wlSurface0.getSurface()).thenReturn(surface0);
        final Rectangle size0 = mock(Rectangle.class);
        when(surface0.getSize()).thenReturn(size0);
        final SurfaceState surfaceState0 = mock(SurfaceState.class);
        when(surface0.getState()).thenReturn(surfaceState0);
        final WlRegionResource wlRegionResource0 = mock(WlRegionResource.class);
        when(surfaceState0.getInputRegion()).thenReturn(Optional.of(wlRegionResource0));
        final WlRegion wlRegion0 = mock(WlRegion.class);
        when(wlRegionResource0.getImplementation()).thenReturn(wlRegion0);
        final Region region0 = mock(Region.class);
        when(wlRegion0.getRegion()).thenReturn(region0);

        //mock surface 0 local coordinates
        final Point localPointerPosition0Start = Point.create(1,1);
        when(surface0.local(this.pointerDevice.getPosition())).thenReturn(localPointerPosition0Start);
        when(region0.contains(size0,
                              localPointerPosition0Start)).thenReturn(true);
        final Point localPointerPosition00 = Point.create(2,3);
        when(surface0.local(eq(pointerPos0))).thenReturn(localPointerPosition00);
        when(region0.contains(eq(size0),
                              eq(localPointerPosition00))).thenReturn(true);
        final Point localPointerPosition01 = Point.create(5,8);
        when(surface0.local(eq(pointerPos1))).thenReturn(localPointerPosition01);
        when(region0.contains(eq(size0),
                              eq(localPointerPosition01))).thenReturn(false);

        //mock surface 1
        final WlSurfaceResource wlSurfaceResource1 = mock(WlSurfaceResource.class);
        wlSurfaceResources.add(wlSurfaceResource1);
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
        final WlRegionResource wlRegionResource1 = mock(WlRegionResource.class);
        when(surfaceState1.getInputRegion()).thenReturn(Optional.of(wlRegionResource1));
        final WlRegion wlRegion1 = mock(WlRegion.class);
        when(wlRegionResource1.getImplementation()).thenReturn(wlRegion1);
        final Region region1 = mock(Region.class);
        when(wlRegion1.getRegion()).thenReturn(region1);

        //mock surface 1 local coordinates
        final Point localPointerPosition1Start = Point.create(13,21);
        when(surface1.local(this.pointerDevice.getPosition())).thenReturn(localPointerPosition1Start);
        when(region1.contains(size1,
                              localPointerPosition1Start)).thenReturn(false);
        final Point localPointerPosition10 = Point.create(34, 55);
        when(surface1.local(eq(pointerPos1))).thenReturn(localPointerPosition10);
        when(region1.contains(eq(size1),
                              eq(localPointerPosition10))).thenReturn(false);
        final Point localPointerPosition11 = Point.create(89, 144);
        when(surface1.local(eq(pointerPos1))).thenReturn(localPointerPosition11);
        when(region1.contains(eq(size1),
                              eq(localPointerPosition11))).thenReturn(true);

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
        when(this.display.nextSerial()).thenReturn(serial0,
                                                   serial1,
                                                   serial2);

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
        //bug in wayland java bindings, we have to use an argument captor to compare Fixed object equality.
        final ArgumentCaptor<Fixed> fixedArgumentCaptor = ArgumentCaptor.forClass(Fixed.class);
        final List<Fixed> values = fixedArgumentCaptor.getAllValues();

        verify(wlPointerResource0).enter(eq(serial0),
                               eq(wlSurfaceResource0),
                               fixedArgumentCaptor.capture(),
                               fixedArgumentCaptor.capture());

        assertThat(values.get(0)
                         .asInt()).isEqualTo(2);
        assertThat(values.get(1)
                         .asInt()).isEqualTo(3);

        verify(wlPointerResource0).motion(eq(time0),
                                fixedArgumentCaptor.capture(),
                                fixedArgumentCaptor.capture());
        assertThat(values.get(2)
                         .asInt()).isEqualTo(2);
        assertThat(values.get(3)
                         .asInt()).isEqualTo(3);


        verify(wlPointerResource0).leave(serial1,
                               wlSurfaceResource0);

        verify(wlPointerResource1).enter(eq(serial2),
                               eq(wlSurfaceResource1),
                               fixedArgumentCaptor.capture(),
                               fixedArgumentCaptor.capture());
        assertThat(values.get(4)
                         .asInt()).isEqualTo(89);
        assertThat(values.get(5)
                         .asInt()).isEqualTo(144);

        verify(wlPointerResource1).motion(eq(time1),
                                fixedArgumentCaptor.capture(),
                                fixedArgumentCaptor.capture());
        assertThat(values.get(6)
                         .asInt()).isEqualTo(89);
        assertThat(values.get(7)
                         .asInt()).isEqualTo(144);
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

        //mock compositor
        final LinkedList<WlSurfaceResource> wlSurfaceResources = new LinkedList<>();
        when(this.compositor.getSurfacesStack()).thenReturn(wlSurfaceResources);

        //mock surface 0
        final WlSurfaceResource wlSurfaceResource0 = mock(WlSurfaceResource.class);
        wlSurfaceResources.add(wlSurfaceResource0);
        final Client client0 = mock(Client.class);
        when(wlSurfaceResource0.getClient()).thenReturn(client0);
        final WlSurface wlSurface0 = mock(WlSurface.class);
        when(wlSurfaceResource0.getImplementation()).thenReturn(wlSurface0);
        final Surface surface0 = mock(Surface.class);
        when(wlSurface0.getSurface()).thenReturn(surface0);
        final Rectangle size0 = mock(Rectangle.class);
        when(surface0.getSize()).thenReturn(size0);
        final SurfaceState surfaceState0 = mock(SurfaceState.class);
        when(surface0.getState()).thenReturn(surfaceState0);
        final WlRegionResource wlRegionResource0 = mock(WlRegionResource.class);
        when(surfaceState0.getInputRegion()).thenReturn(Optional.of(wlRegionResource0));
        final WlRegion wlRegion0 = mock(WlRegion.class);
        when(wlRegionResource0.getImplementation()).thenReturn(wlRegion0);
        final Region region0 = mock(Region.class);
        when(wlRegion0.getRegion()).thenReturn(region0);

        //mock surface 0 local coordinates
        final Point localPointerPosition0Start = Point.create(1,1);
        when(surface0.local(this.pointerDevice.getPosition())).thenReturn(localPointerPosition0Start);
        when(region0.contains(size0,
                              localPointerPosition0Start)).thenReturn(true);
        final Point localPointerPosition00 = Point.create(2,3);
        when(surface0.local(eq(pointerPos0))).thenReturn(localPointerPosition00);
        when(region0.contains(eq(size0),
                              eq(localPointerPosition00))).thenReturn(true);
        final Point localPointerPosition01 = Point.create(5,8);
        when(surface0.local(eq(pointerPos1))).thenReturn(localPointerPosition01);
        when(region0.contains(eq(size0),
                              eq(localPointerPosition01))).thenReturn(false);

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
        //bug in wayland java bindings, we have to use an argument captor to compare Fixed object equality.
        final ArgumentCaptor<Fixed> fixedArgumentCaptor = ArgumentCaptor.forClass(Fixed.class);
        final List<Fixed> values = fixedArgumentCaptor.getAllValues();

        verify(wlPointerResource0).enter(eq(serial0),
                               eq(wlSurfaceResource0),
                               fixedArgumentCaptor.capture(),
                               fixedArgumentCaptor.capture());

        assertThat(values.get(0)
                         .asInt()).isEqualTo(2);
        assertThat(values.get(1)
                         .asInt()).isEqualTo(3);

        verify(wlPointerResource0).motion(eq(time0),
                                fixedArgumentCaptor.capture(),
                                fixedArgumentCaptor.capture());
        assertThat(values.get(2)
                         .asInt()).isEqualTo(2);
        assertThat(values.get(3)
                         .asInt()).isEqualTo(3);

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

        //mock compositor
        final LinkedList<WlSurfaceResource> wlSurfaceResources = new LinkedList<>();
        when(this.compositor.getSurfacesStack()).thenReturn(wlSurfaceResources);

        //mock surface 0
        final WlSurfaceResource wlSurfaceResource0 = mock(WlSurfaceResource.class);
        wlSurfaceResources.add(wlSurfaceResource0);
        final Client client0 = mock(Client.class);
        when(wlSurfaceResource0.getClient()).thenReturn(client0);
        final WlSurface wlSurface0 = mock(WlSurface.class);
        when(wlSurfaceResource0.getImplementation()).thenReturn(wlSurface0);
        final Surface surface0 = mock(Surface.class);
        when(wlSurface0.getSurface()).thenReturn(surface0);
        final Rectangle size0 = mock(Rectangle.class);
        when(surface0.getSize()).thenReturn(size0);
        final SurfaceState surfaceState0 = mock(SurfaceState.class);
        when(surface0.getState()).thenReturn(surfaceState0);
        final WlRegionResource wlRegionResource0 = mock(WlRegionResource.class);
        when(surfaceState0.getInputRegion()).thenReturn(Optional.of(wlRegionResource0));
        final WlRegion wlRegion0 = mock(WlRegion.class);
        when(wlRegionResource0.getImplementation()).thenReturn(wlRegion0);
        final Region region0 = mock(Region.class);
        when(wlRegion0.getRegion()).thenReturn(region0);

        //mock surface 0 local coordinates
        final Point localPointerPosition0Start = Point.create(1,1);
        when(surface0.local(this.pointerDevice.getPosition())).thenReturn(localPointerPosition0Start);
        when(region0.contains(size0,
                              localPointerPosition0Start)).thenReturn(false);
        final Point localPointerPosition00 = Point.create(2,3);
        when(surface0.local(eq(pointerPos0))).thenReturn(localPointerPosition00);
        when(region0.contains(eq(size0),
                              eq(localPointerPosition00))).thenReturn(false);
        final Point localPointerPosition01 = Point.create(5,8);
        when(surface0.local(eq(pointerPos1))).thenReturn(localPointerPosition01);
        when(region0.contains(eq(size0),
                              eq(localPointerPosition01))).thenReturn(true);

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
        //bug in wayland java bindings, we have to use an argument captor to compare Fixed object equality.
        final ArgumentCaptor<Fixed> fixedArgumentCaptor = ArgumentCaptor.forClass(Fixed.class);
        final List<Fixed> values = fixedArgumentCaptor.getAllValues();

        verify(wlPointerResource0).enter(eq(serial0),
                               eq(wlSurfaceResource0),
                               fixedArgumentCaptor.capture(),
                               fixedArgumentCaptor.capture());

        assertThat(values.get(0)
                         .asInt()).isEqualTo(5);
        assertThat(values.get(1)
                         .asInt()).isEqualTo(8);

        verify(wlPointerResource0).motion(eq(time1),
                                fixedArgumentCaptor.capture(),
                                fixedArgumentCaptor.capture());
        assertThat(values.get(2)
                         .asInt()).isEqualTo(5);
        assertThat(values.get(3)
                         .asInt()).isEqualTo(8);
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

        //mock compositor
        final LinkedList<WlSurfaceResource> wlSurfaceResources = new LinkedList<>();
        when(this.compositor.getSurfacesStack()).thenReturn(wlSurfaceResources);

        //mock surface 0
        final WlSurfaceResource wlSurfaceResource0 = mock(WlSurfaceResource.class);
        wlSurfaceResources.add(wlSurfaceResource0);
        final Client client0 = mock(Client.class);
        when(wlSurfaceResource0.getClient()).thenReturn(client0);
        final WlSurface wlSurface0 = mock(WlSurface.class);
        when(wlSurfaceResource0.getImplementation()).thenReturn(wlSurface0);
        final Surface surface0 = mock(Surface.class);
        when(wlSurface0.getSurface()).thenReturn(surface0);
        final Rectangle size0 = mock(Rectangle.class);
        when(surface0.getSize()).thenReturn(size0);
        final SurfaceState surfaceState0 = mock(SurfaceState.class);
        when(surface0.getState()).thenReturn(surfaceState0);
        final WlRegionResource wlRegionResource0 = mock(WlRegionResource.class);
        when(surfaceState0.getInputRegion()).thenReturn(Optional.of(wlRegionResource0));
        final WlRegion wlRegion0 = mock(WlRegion.class);
        when(wlRegionResource0.getImplementation()).thenReturn(wlRegion0);
        final Region region0 = mock(Region.class);
        when(wlRegion0.getRegion()).thenReturn(region0);

        //mock surface 0 local coordinates
        final Point localPointerPosition0Start = Point.create(1,1);
        when(surface0.local(this.pointerDevice.getPosition())).thenReturn(localPointerPosition0Start);
        when(region0.contains(size0,
                              localPointerPosition0Start)).thenReturn(false);
        final Point localPointerPosition00 = Point.create(2,3);
        when(surface0.local(eq(pointerPos0))).thenReturn(localPointerPosition00);
        when(region0.contains(eq(size0),
                              eq(localPointerPosition00))).thenReturn(false);
        final Point localPointerPosition01 = Point.create(5,8);
        when(surface0.local(eq(pointerPos1))).thenReturn(localPointerPosition01);
        when(region0.contains(eq(size0),
                              eq(localPointerPosition01))).thenReturn(true);

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
        //bug in wayland java bindings, we have to use an argument captor to compare Fixed object equality.
        final ArgumentCaptor<Fixed> fixedArgumentCaptor = ArgumentCaptor.forClass(Fixed.class);
        final List<Fixed> values = fixedArgumentCaptor.getAllValues();

        verify(wlPointerResource0).enter(eq(serial0),
                               eq(wlSurfaceResource0),
                               fixedArgumentCaptor.capture(),
                               fixedArgumentCaptor.capture());

        assertThat(values.get(0)
                         .asInt()).isEqualTo(5);
        assertThat(values.get(1)
                         .asInt()).isEqualTo(8);

        verify(wlPointerResource0).motion(eq(time2),
                                fixedArgumentCaptor.capture(),
                                fixedArgumentCaptor.capture());
        assertThat(values.get(2)
                         .asInt()).isEqualTo(5);
        assertThat(values.get(3)
                         .asInt()).isEqualTo(8);

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

        //mock compositor
        final LinkedList<WlSurfaceResource> wlSurfaceResources = new LinkedList<>();
        when(this.compositor.getSurfacesStack()).thenReturn(wlSurfaceResources);

        //mock surface 0
        final WlSurfaceResource wlSurfaceResource0 = mock(WlSurfaceResource.class);
        wlSurfaceResources.add(wlSurfaceResource0);
        final Client client0 = mock(Client.class);
        when(wlSurfaceResource0.getClient()).thenReturn(client0);
        final WlSurface wlSurface0 = mock(WlSurface.class);
        when(wlSurfaceResource0.getImplementation()).thenReturn(wlSurface0);
        final Surface surface0 = mock(Surface.class);
        when(wlSurface0.getSurface()).thenReturn(surface0);
        final Rectangle size0 = mock(Rectangle.class);
        when(surface0.getSize()).thenReturn(size0);
        final SurfaceState surfaceState0 = mock(SurfaceState.class);
        when(surface0.getState()).thenReturn(surfaceState0);
        final WlRegionResource wlRegionResource0 = mock(WlRegionResource.class);
        when(surfaceState0.getInputRegion()).thenReturn(Optional.of(wlRegionResource0));
        final WlRegion wlRegion0 = mock(WlRegion.class);
        when(wlRegionResource0.getImplementation()).thenReturn(wlRegion0);
        final Region region0 = mock(Region.class);
        when(wlRegion0.getRegion()).thenReturn(region0);

        //mock surface 0 local coordinates
        final Point localPointerPosition0Start = Point.create(1,1);
        when(surface0.local(this.pointerDevice.getPosition())).thenReturn(localPointerPosition0Start);
        when(region0.contains(size0,
                              localPointerPosition0Start)).thenReturn(true);
        final Point localPointerPosition00 = Point.create(2,3);
        when(surface0.local(eq(pointerPos0))).thenReturn(localPointerPosition00);
        when(region0.contains(eq(size0),
                              eq(localPointerPosition00))).thenReturn(true);
        final Point localPointerPosition01 = Point.create(5,8);
        when(surface0.local(eq(pointerPos1))).thenReturn(localPointerPosition01);
        when(region0.contains(eq(size0),
                              eq(localPointerPosition01))).thenReturn(true);

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
        //bug in wayland java bindings, we have to use an argument captor to compare Fixed object equality.
        final ArgumentCaptor<Fixed> fixedArgumentCaptor = ArgumentCaptor.forClass(Fixed.class);
        final List<Fixed> values = fixedArgumentCaptor.getAllValues();

        verify(wlPointerResource0).enter(eq(serial0),
                               eq(wlSurfaceResource0),
                               fixedArgumentCaptor.capture(),
                               fixedArgumentCaptor.capture());

        assertThat(values.get(0)
                         .asInt()).isEqualTo(2);
        assertThat(values.get(1)
                         .asInt()).isEqualTo(3);

        verify(wlPointerResource0).motion(eq(time0),
                                fixedArgumentCaptor.capture(),
                                fixedArgumentCaptor.capture());
        assertThat(values.get(2)
                         .asInt()).isEqualTo(2);
        assertThat(values.get(3)
                         .asInt()).isEqualTo(3);

        verify(wlPointerResource0).button(serial1,
                                time1,
                                button0,
                                WlPointerButtonState.PRESSED.getValue());

        verify(wlPointerResource0).button(serial2,
                                time2,
                                button0,
                                WlPointerButtonState.RELEASED.getValue());
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

        //mock compositor
        final LinkedList<WlSurfaceResource> wlSurfaceResources = new LinkedList<>();
        when(this.compositor.getSurfacesStack()).thenReturn(wlSurfaceResources);

        //mock surface 0
        final WlSurfaceResource wlSurfaceResource0 = mock(WlSurfaceResource.class);
        wlSurfaceResources.add(wlSurfaceResource0);
        final Client client0 = mock(Client.class);
        when(wlSurfaceResource0.getClient()).thenReturn(client0);
        final WlSurface wlSurface0 = mock(WlSurface.class);
        when(wlSurfaceResource0.getImplementation()).thenReturn(wlSurface0);
        final Surface surface0 = mock(Surface.class);
        when(wlSurface0.getSurface()).thenReturn(surface0);
        final Rectangle size0 = mock(Rectangle.class);
        when(surface0.getSize()).thenReturn(size0);
        final SurfaceState surfaceState0 = mock(SurfaceState.class);
        when(surface0.getState()).thenReturn(surfaceState0);
        final WlRegionResource wlRegionResource0 = mock(WlRegionResource.class);
        when(surfaceState0.getInputRegion()).thenReturn(Optional.of(wlRegionResource0));
        final WlRegion wlRegion0 = mock(WlRegion.class);
        when(wlRegionResource0.getImplementation()).thenReturn(wlRegion0);
        final Region region0 = mock(Region.class);
        when(wlRegion0.getRegion()).thenReturn(region0);

        //mock surface 0 local coordinates
        final Point localPointerPosition0Start = Point.create(1,1);
        when(surface0.local(this.pointerDevice.getPosition())).thenReturn(localPointerPosition0Start);
        when(region0.contains(size0,
                              localPointerPosition0Start)).thenReturn(true);
        final Point localPointerPosition00 = Point.create(2,3);
        when(surface0.local(eq(pointerPos0))).thenReturn(localPointerPosition00);
        when(region0.contains(eq(size0),
                              eq(localPointerPosition00))).thenReturn(true);
        final Point localPointerPosition01 = Point.create(5,8);
        when(surface0.local(eq(pointerPos1))).thenReturn(localPointerPosition01);
        when(region0.contains(eq(size0),
                              eq(localPointerPosition01))).thenReturn(true);

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
    public void testOver() throws Exception {
        //given
        final LinkedList<WlSurfaceResource> wlSurfaceResources = new LinkedList<>();

        final WlSurfaceResource wlSurfaceResource0 = mock(WlSurfaceResource.class);
        wlSurfaceResources.add(wlSurfaceResource0);
        final WlSurface wlSurface0 = mock(WlSurface.class);
        when(wlSurfaceResource0.getImplementation()).thenReturn(wlSurface0);
        final Surface surface0 = mock(Surface.class);
        when(wlSurface0.getSurface()).thenReturn(surface0);
        final Rectangle size0 = mock(Rectangle.class);
        when(surface0.getSize()).thenReturn(size0);
        final SurfaceState surfaceState0 = mock(SurfaceState.class);
        when(surface0.getState()).thenReturn(surfaceState0);
        final WlRegionResource wlRegionResource0 = mock(WlRegionResource.class);
        when(surfaceState0.getInputRegion()).thenReturn(Optional.of(wlRegionResource0));
        final WlRegion wlRegion0 = mock(WlRegion.class);
        when(wlRegionResource0.getImplementation()).thenReturn(wlRegion0);
        final Region region0 = mock(Region.class);
        when(wlRegion0.getRegion()).thenReturn(region0);
        final Point localPointerPosition0 = mock(Point.class);
        when(surface0.local(this.pointerDevice.getPosition())).thenReturn(localPointerPosition0);
        when(region0.contains(size0,
                              localPointerPosition0)).thenReturn(true);

        final WlSurfaceResource wlSurfaceResource1 = mock(WlSurfaceResource.class);
        wlSurfaceResources.add(wlSurfaceResource1);
        final WlSurface wlSurface1 = mock(WlSurface.class);
        when(wlSurfaceResource1.getImplementation()).thenReturn(wlSurface1);
        final Surface surface1 = mock(Surface.class);
        when(wlSurface1.getSurface()).thenReturn(surface1);
        final Rectangle size1 = mock(Rectangle.class);
        when(surface1.getSize()).thenReturn(size1);
        final SurfaceState surfaceState1 = mock(SurfaceState.class);
        when(surface1.getState()).thenReturn(surfaceState1);
        final WlRegionResource wlRegionResource1 = mock(WlRegionResource.class);
        when(surfaceState1.getInputRegion()).thenReturn(Optional.of(wlRegionResource1));
        final WlRegion wlRegion1 = mock(WlRegion.class);
        when(wlRegionResource1.getImplementation()).thenReturn(wlRegion1);
        final Region region1 = mock(Region.class);
        when(wlRegion1.getRegion()).thenReturn(region1);
        final Point localPointerPosition1 = mock(Point.class);
        when(surface1.local(this.pointerDevice.getPosition())).thenReturn(localPointerPosition1);
        when(region1.contains(size1,
                              localPointerPosition1)).thenReturn(false);

        when(this.compositor.getSurfacesStack()).thenReturn(wlSurfaceResources);

        //when
        final Optional<WlSurfaceResource> over = this.pointerDevice.over();

        //then
        assertThat(over.get()).isEqualTo(wlSurfaceResource0);
    }

    @Test
    public void testGrabMotion() throws Exception {
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

        //mock compositor
        final LinkedList<WlSurfaceResource> wlSurfaceResources = new LinkedList<>();
        when(this.compositor.getSurfacesStack()).thenReturn(wlSurfaceResources);

        //mock surface 0
        final WlSurfaceResource wlSurfaceResource0 = mock(WlSurfaceResource.class);
        wlSurfaceResources.add(wlSurfaceResource0);
        final Client client0 = mock(Client.class);
        when(wlSurfaceResource0.getClient()).thenReturn(client0);
        final WlSurface wlSurface0 = mock(WlSurface.class);
        when(wlSurfaceResource0.getImplementation()).thenReturn(wlSurface0);
        final Surface surface0 = mock(Surface.class);
        when(wlSurface0.getSurface()).thenReturn(surface0);
        final Rectangle size0 = mock(Rectangle.class);
        when(surface0.getSize()).thenReturn(size0);
        final SurfaceState surfaceState0 = mock(SurfaceState.class);
        when(surface0.getState()).thenReturn(surfaceState0);
        final WlRegionResource wlRegionResource0 = mock(WlRegionResource.class);
        when(surfaceState0.getInputRegion()).thenReturn(Optional.of(wlRegionResource0));
        final WlRegion wlRegion0 = mock(WlRegion.class);
        when(wlRegionResource0.getImplementation()).thenReturn(wlRegion0);
        final Region region0 = mock(Region.class);
        when(wlRegion0.getRegion()).thenReturn(region0);

        //mock surface 0 local coordinates
        final Point localPointerPosition0Start = Point.create(1,1);
        when(surface0.local(this.pointerDevice.getPosition())).thenReturn(localPointerPosition0Start);
        when(region0.contains(size0,
                              localPointerPosition0Start)).thenReturn(true);
        final Point localPointerPosition00 = Point.create(2,3);
        when(surface0.local(eq(pointerPos0))).thenReturn(localPointerPosition00);
        when(region0.contains(eq(size0),
                              eq(localPointerPosition00))).thenReturn(true);
        final Point localPointerPosition01 = Point.create(5,8);
        when(surface0.local(eq(pointerPos1))).thenReturn(localPointerPosition01);
        when(region0.contains(eq(size0),
                              eq(localPointerPosition01))).thenReturn(true);

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
        verify(pointerGrabMotion).motion(eq(Motion.create(time3,
                                                          x1,
                                                          y1)));
        final ArgumentCaptor<Listener> listenerArgumentCaptor = ArgumentCaptor.forClass(Listener.class);
        verify(wlSurfaceResource0).addDestroyListener(listenerArgumentCaptor.capture());
        //and when
        final Listener listener = listenerArgumentCaptor.getValue();
        listener.handle();
        this.pointerDevice.motion(pointerResources,
                                  time3,
                                  x1,
                                  y1);
        //then
        verifyNoMoreInteractions(pointerGrabMotion);
    }
}