package org.westmalle.wayland.output;

import org.freedesktop.wayland.server.*;
import org.freedesktop.wayland.shared.WlPointerButtonState;
import org.freedesktop.wayland.util.Fixed;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.westmalle.wayland.protocol.WlRegion;
import org.westmalle.wayland.protocol.WlSurface;

import javax.media.nativewindow.util.Point;
import javax.media.nativewindow.util.PointImmutable;
import java.util.*;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PointerDeviceTest {

    @Mock
    private Display       display;
    @Mock
    private Compositor    compositor;
    @InjectMocks
    private PointerDevice pointerDevice;

    /**
     * cursor moves from one surface to another surface while button is pressed
     */
    @Test
    public void testGrabNewFocusMotion() throws Exception {
        //given
        final Set<WlPointerResource> pointerResources = new HashSet<>();
        final WlPointerResource wlPointerResource0 = mock(WlPointerResource.class);
        final WlPointerResource wlPointerResource1 = mock(WlPointerResource.class);
        pointerResources.add(wlPointerResource0);
        pointerResources.add(wlPointerResource1);
        final int time = 112358;
        final int x0 = 20;
        final int y0 = 30;
        final PointImmutable pos0 = new Point(x0,
                                              y0);

        final int x1 = 500;
        final int y1 = 600;
        final PointImmutable pos1 = new Point(x1,
                                              y1);

        final int button0 = 1;

        final Client client0 = mock(Client.class);
        final Client client1 = mock(Client.class);

        final WlSurfaceResource wlSurfaceResource0 = mock(WlSurfaceResource.class);
        final WlSurfaceResource wlSurfaceResource1 = mock(WlSurfaceResource.class);

        final LinkedList<WlSurfaceResource> surfaceResources = new LinkedList<>();
        surfaceResources.add(wlSurfaceResource0);
        surfaceResources.add(wlSurfaceResource1);
        when(this.compositor.getSurfacesStack()).thenReturn(surfaceResources);

        final WlSurface wlSurface0 = mock(WlSurface.class);
        when(wlSurfaceResource0.getImplementation()).thenReturn(wlSurface0);
        final WlSurface wlSurface1 = mock(WlSurface.class);
        when(wlSurfaceResource1.getImplementation()).thenReturn(wlSurface1);

        final Surface surface0 = mock(Surface.class);
        when(wlSurface0.getSurface()).thenReturn(surface0);
        final Surface surface1 = mock(Surface.class);
        when(wlSurface1.getSurface()).thenReturn(surface1);

        WlRegionResource wlRegionResource0 = mock(WlRegionResource.class);
        when(surface0.getInputRegion()).thenReturn(Optional.of(wlRegionResource0));
        WlRegionResource wlRegionResource1 = mock(WlRegionResource.class);
        when(surface1.getInputRegion()).thenReturn(Optional.of(wlRegionResource1));

        final WlRegion wlRegion0 = mock(WlRegion.class);
        when(wlRegionResource0.getImplementation()).thenReturn(wlRegion0);
        final WlRegion wlRegion1 = mock(WlRegion.class);
        when(wlRegionResource1.getImplementation()).thenReturn(wlRegion1);

        final Region region0 = mock(Region.class);
        when(wlRegion0.getRegion()).thenReturn(region0);
        final Region region1 = mock(Region.class);
        when(wlRegion1.getRegion()).thenReturn(region1);

        when(wlSurfaceResource0.getClient()).thenReturn(client0);
        when(wlSurfaceResource1.getClient()).thenReturn(client1);

        when(wlPointerResource0.getClient()).thenReturn(client0);
        when(wlPointerResource1.getClient()).thenReturn(client1);

        final PointImmutable surfacePos0 = new Point(10,
                                                     10);
        final int relX0 = x0 - 10;
        final int relY0 = y0 - 10;
        final PointImmutable relPos0 = new Point(relX0,
                                                 relY0);
        when(surface0.relativeCoordinate(pos0)).thenReturn(relPos0);
        when(region0.contains(relPos0)).thenReturn(true);
        when(surface0.getPosition()).thenReturn(surfacePos0);

        final PointImmutable surfacePos1 = new Point(400,
                                                     400);
        final int relX1 = x1 - 400;
        final int relY1 = y1 - 400;
        final PointImmutable relPos1 = new Point(relX1,
                                                 relY1);
        when(surface1.relativeCoordinate(pos1)).thenReturn(relPos1);
        when(region1.contains(relPos1)).thenReturn(true);
        when(surface1.getPosition()).thenReturn(surfacePos1);

        final int relX3 = x0 - 400;
        final int relY3 = y0 - 400;
        final PointImmutable relPos3 = new Point(relX3,
                                                 relY3);
        when(surface0.relativeCoordinate(pos1)).thenReturn(relPos3);

        final int serial = 90879;
        when(this.display.nextSerial()).thenReturn(serial);
        //when
        this.pointerDevice.motion(pointerResources,
                                  time,
                                  x0,
                                  y0);
        this.pointerDevice.button(pointerResources,
                                  time,
                                  button0,
                                  WlPointerButtonState.PRESSED);
        this.pointerDevice.motion(pointerResources,
                                  time,
                                  x1,
                                  y1);
        //then
        //bug in wayland java bindings, we have to use an argument captor to compare Fixed object equality.
        ArgumentCaptor<Fixed> fixedArgumentCaptor = ArgumentCaptor.forClass(Fixed.class);
        final List<Fixed> values = fixedArgumentCaptor.getAllValues();

        verify(wlPointerResource0,
               times(1)).enter(eq(this.display.nextSerial()),
                               eq(wlSurfaceResource0),
                               fixedArgumentCaptor.capture(),
                               fixedArgumentCaptor.capture());

        assertThat(values.get(0)
                         .asInt()).isEqualTo(relX0);
        assertThat(values.get(1)
                         .asInt()).isEqualTo(relY0);

        verify(wlPointerResource0,
               times(2)).motion(eq(time),
                                fixedArgumentCaptor.capture(),
                                fixedArgumentCaptor.capture());
        assertThat(values.get(2)
                         .asInt()).isEqualTo(relX0);
        assertThat(values.get(3)
                         .asInt()).isEqualTo(relY0);
        assertThat(values.get(4)
                         .asInt()).isEqualTo(relX3);
        assertThat(values.get(5)
                         .asInt()).isEqualTo(relY3);

        verify(wlPointerResource0,
               times(1)).button(serial,
                                time,
                                button0,
                                WlPointerButtonState.PRESSED.getValue());

        verify(wlPointerResource0,
               times(1)).leave(this.display.nextSerial(),
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
        final Set<WlPointerResource> pointerResources = new HashSet<>();
        final WlPointerResource wlPointerResource0 = mock(WlPointerResource.class);
        final WlPointerResource wlPointerResource1 = mock(WlPointerResource.class);
        pointerResources.add(wlPointerResource0);
        pointerResources.add(wlPointerResource1);
        final int time = 112358;
        final int x0 = 20;
        final int y0 = 30;
        final PointImmutable pos0 = new Point(x0,
                                              y0);

        final int x1 = 100;
        final int y1 = 200;
        final PointImmutable pos1 = new Point(x1,
                                              y1);

        final Client client0 = mock(Client.class);
        final Client client1 = mock(Client.class);

        final WlSurfaceResource wlSurfaceResource0 = mock(WlSurfaceResource.class);
        final WlSurfaceResource wlSurfaceResource1 = mock(WlSurfaceResource.class);

        final LinkedList<WlSurfaceResource> surfaceResources = new LinkedList<>();
        surfaceResources.add(wlSurfaceResource0);
        surfaceResources.add(wlSurfaceResource1);
        when(this.compositor.getSurfacesStack()).thenReturn(surfaceResources);

        final WlSurface wlSurface0 = mock(WlSurface.class);
        when(wlSurfaceResource0.getImplementation()).thenReturn(wlSurface0);
        final WlSurface wlSurface1 = mock(WlSurface.class);
        when(wlSurfaceResource1.getImplementation()).thenReturn(wlSurface1);

        final Surface surface0 = mock(Surface.class);
        when(wlSurface0.getSurface()).thenReturn(surface0);
        final Surface surface1 = mock(Surface.class);
        when(wlSurface1.getSurface()).thenReturn(surface1);

        WlRegionResource wlRegionResource0 = mock(WlRegionResource.class);
        when(surface0.getInputRegion()).thenReturn(Optional.of(wlRegionResource0));
        WlRegionResource wlRegionResource1 = mock(WlRegionResource.class);
        when(surface1.getInputRegion()).thenReturn(Optional.of(wlRegionResource1));

        final WlRegion wlRegion0 = mock(WlRegion.class);
        when(wlRegionResource0.getImplementation()).thenReturn(wlRegion0);
        final WlRegion wlRegion1 = mock(WlRegion.class);
        when(wlRegionResource1.getImplementation()).thenReturn(wlRegion1);

        final Region region0 = mock(Region.class);
        when(wlRegion0.getRegion()).thenReturn(region0);
        final Region region1 = mock(Region.class);
        when(wlRegion1.getRegion()).thenReturn(region1);

        when(wlSurfaceResource0.getClient()).thenReturn(client0);
        when(wlSurfaceResource1.getClient()).thenReturn(client1);

        when(wlPointerResource0.getClient()).thenReturn(client0);
        when(wlPointerResource1.getClient()).thenReturn(client1);

        final int relX0 = 50;
        final int relY0 = 100;
        final PointImmutable relPos0 = new Point(relX0,
                                                 relY0);
        when(surface0.relativeCoordinate(pos0)).thenReturn(relPos0);
        when(region0.contains(relPos0)).thenReturn(true);

        final PointImmutable surfacePos0 = new Point(x0 - relX0,
                                                     y0 - relY0);
        when(surface0.getPosition()).thenReturn(surfacePos0);

        final int relX1 = 0;
        final int relY1 = 100;
        final PointImmutable relPos1 = new Point(relX1,
                                                 relY1);
        when(surface1.relativeCoordinate(pos1)).thenReturn(relPos1);
        when(region1.contains(relPos1)).thenReturn(true);

        final PointImmutable surfacePos1 = new Point(x1 - relX1,
                                                     y1 - relY1);
        when(surface1.getPosition()).thenReturn(surfacePos1);

        final int serial0 = 90879;
        final int serial1 = 90880;
        final int serial2 = 90881;
        when(this.display.nextSerial()).thenReturn(serial0,
                                                   serial1,
                                                   serial2);
        //when
        this.pointerDevice.motion(pointerResources,
                                  time,
                                  x0,
                                  y0);
        this.pointerDevice.motion(pointerResources,
                                  time,
                                  x1,
                                  y1);
        //then
        //bug in wayland java bindings, we have to use an argument captor to compare Fixed object equality.
        ArgumentCaptor<Fixed> fixedArgumentCaptor = ArgumentCaptor.forClass(Fixed.class);
        final List<Fixed> values = fixedArgumentCaptor.getAllValues();

        verify(wlPointerResource0,
               times(1)).enter(eq(serial0),
                               eq(wlSurfaceResource0),
                               fixedArgumentCaptor.capture(),
                               fixedArgumentCaptor.capture());

        assertThat(values.get(0)
                         .asInt()).isEqualTo(relX0);
        assertThat(values.get(1)
                         .asInt()).isEqualTo(relY0);

        verify(wlPointerResource0,
               times(1)).motion(eq(time),
                                fixedArgumentCaptor.capture(),
                                fixedArgumentCaptor.capture());
        assertThat(values.get(2)
                         .asInt()).isEqualTo(relX0);
        assertThat(values.get(3)
                         .asInt()).isEqualTo(relY0);


        verify(wlPointerResource0,
               times(1)).leave(serial1,
                               wlSurfaceResource0);

        verify(wlPointerResource1,
               times(1)).enter(eq(serial2),
                               eq(wlSurfaceResource1),
                               fixedArgumentCaptor.capture(),
                               fixedArgumentCaptor.capture());
        assertThat(values.get(4)
                         .asInt()).isEqualTo(relX1);
        assertThat(values.get(5)
                         .asInt()).isEqualTo(relY1);

        verify(wlPointerResource1,
               times(1)).motion(eq(time),
                                fixedArgumentCaptor.capture(),
                                fixedArgumentCaptor.capture());
        assertThat(values.get(6)
                         .asInt()).isEqualTo(relX1);
        assertThat(values.get(7)
                         .asInt()).isEqualTo(relY1);
    }

    /**
     * cursor moves from surface to no surface, no button is pressed
     */
    @Test
    public void testNoFocusMotion() throws Exception {
        //given
        final Set<WlPointerResource> pointerResources = new HashSet<>();
        final WlPointerResource wlPointerResource0 = mock(WlPointerResource.class);
        pointerResources.add(wlPointerResource0);
        final int time = 112358;
        final int x0 = 20;
        final int y0 = 30;
        final PointImmutable pos0 = new Point(x0,
                                              y0);

        final int x1 = 100;
        final int y1 = 200;
        final PointImmutable pos1 = new Point(x1,
                                              y1);

        final Client client0 = mock(Client.class);

        final WlSurfaceResource wlSurfaceResource0 = mock(WlSurfaceResource.class);

        final LinkedList<WlSurfaceResource> surfaceResources = new LinkedList<>();
        surfaceResources.add(wlSurfaceResource0);
        when(this.compositor.getSurfacesStack()).thenReturn(surfaceResources);

        final WlSurface wlSurface0 = mock(WlSurface.class);
        when(wlSurfaceResource0.getImplementation()).thenReturn(wlSurface0);

        final Surface surface0 = mock(Surface.class);
        when(wlSurface0.getSurface()).thenReturn(surface0);

        WlRegionResource wlRegionResource0 = mock(WlRegionResource.class);
        when(surface0.getInputRegion()).thenReturn(Optional.of(wlRegionResource0));

        final WlRegion wlRegion0 = mock(WlRegion.class);
        when(wlRegionResource0.getImplementation()).thenReturn(wlRegion0);

        final Region region0 = mock(Region.class);
        when(wlRegion0.getRegion()).thenReturn(region0);

        when(wlSurfaceResource0.getClient()).thenReturn(client0);

        when(wlPointerResource0.getClient()).thenReturn(client0);

        final int relX0 = 50;
        final int relY0 = 100;
        final PointImmutable relPos0 = new Point(relX0,
                                                 relY0);
        when(surface0.relativeCoordinate(pos0)).thenReturn(relPos0);
        when(region0.contains(relPos0)).thenReturn(true);

        final PointImmutable surfacePos0 = new Point(x0 - relX0,
                                                     y0 - relY0);
        when(surface0.getPosition()).thenReturn(surfacePos0);

        final int relX1 = 0;
        final int relY1 = 100;
        final PointImmutable relPos1 = new Point(relX1,
                                                 relY1);
        when(surface0.relativeCoordinate(pos1)).thenReturn(relPos1);
        when(region0.contains(relPos1)).thenReturn(false);

        final int serial0 = 90879;
        final int serial1 = 90880;
        final int serial2 = 90881;
        when(this.display.nextSerial()).thenReturn(serial0,
                                                   serial1,
                                                   serial2);
        //when
        this.pointerDevice.motion(pointerResources,
                                  time,
                                  x0,
                                  y0);
        this.pointerDevice.motion(pointerResources,
                                  time,
                                  x1,
                                  y1);
        //then
        //bug in wayland java bindings, we have to use an argument captor to compare Fixed object equality.
        ArgumentCaptor<Fixed> fixedArgumentCaptor = ArgumentCaptor.forClass(Fixed.class);
        final List<Fixed> values = fixedArgumentCaptor.getAllValues();

        verify(wlPointerResource0,
               times(1)).enter(eq(serial0),
                               eq(wlSurfaceResource0),
                               fixedArgumentCaptor.capture(),
                               fixedArgumentCaptor.capture());

        assertThat(values.get(0)
                         .asInt()).isEqualTo(relX0);
        assertThat(values.get(1)
                         .asInt()).isEqualTo(relY0);

        verify(wlPointerResource0,
               times(1)).motion(eq(time),
                                fixedArgumentCaptor.capture(),
                                fixedArgumentCaptor.capture());
        assertThat(values.get(2)
                         .asInt()).isEqualTo(relX0);
        assertThat(values.get(3)
                         .asInt()).isEqualTo(relY0);

        verify(wlPointerResource0,
               times(1)).leave(serial1,
                               wlSurfaceResource0);
    }

    /**
     * cursor moves from no surface into surface, nu button is pressed
     */
    @Test
    public void testNewFocusMotion() throws Exception {
        //given
        final Set<WlPointerResource> pointerResources = new HashSet<>();
        final WlPointerResource wlPointerResource0 = mock(WlPointerResource.class);
        pointerResources.add(wlPointerResource0);
        final int time = 112358;
        final int x0 = 20;
        final int y0 = 30;
        final PointImmutable pos0 = new Point(x0,
                                              y0);

        final int x1 = 100;
        final int y1 = 200;
        final PointImmutable pos1 = new Point(x1,
                                              y1);

        final Client client0 = mock(Client.class);

        final WlSurfaceResource wlSurfaceResource0 = mock(WlSurfaceResource.class);

        final LinkedList<WlSurfaceResource> surfaceResources = new LinkedList<>();
        surfaceResources.add(wlSurfaceResource0);
        when(this.compositor.getSurfacesStack()).thenReturn(surfaceResources);

        final WlSurface wlSurface0 = mock(WlSurface.class);
        when(wlSurfaceResource0.getImplementation()).thenReturn(wlSurface0);

        final Surface surface0 = mock(Surface.class);
        when(wlSurface0.getSurface()).thenReturn(surface0);

        WlRegionResource wlRegionResource0 = mock(WlRegionResource.class);
        when(surface0.getInputRegion()).thenReturn(Optional.of(wlRegionResource0));

        final WlRegion wlRegion0 = mock(WlRegion.class);
        when(wlRegionResource0.getImplementation()).thenReturn(wlRegion0);

        final Region region0 = mock(Region.class);
        when(wlRegion0.getRegion()).thenReturn(region0);

        when(wlSurfaceResource0.getClient()).thenReturn(client0);

        when(wlPointerResource0.getClient()).thenReturn(client0);

        final PointImmutable surfacePos0 = new Point(100,
                                                     100);
        final int relX0 = x0 - 100;
        final int relY0 = y0 - 100;
        final PointImmutable relPos0 = new Point(relX0,
                                                 relY0);
        when(surface0.relativeCoordinate(pos0)).thenReturn(relPos0);
        when(region0.contains(relPos0)).thenReturn(false);
        when(surface0.getPosition()).thenReturn(surfacePos0);

        final int relX1 = x1 - 100;
        final int relY1 = y1 - 100;
        final PointImmutable relPos1 = new Point(relX1,
                                                 relY1);
        when(surface0.relativeCoordinate(pos1)).thenReturn(relPos1);
        when(region0.contains(relPos1)).thenReturn(true);

        final int serial0 = 90879;
        final int serial1 = 90880;
        final int serial2 = 90881;
        when(this.display.nextSerial()).thenReturn(serial0,
                                                   serial1,
                                                   serial2);
        //when
        this.pointerDevice.motion(pointerResources,
                                  time,
                                  x0,
                                  y0);
        this.pointerDevice.motion(pointerResources,
                                  time,
                                  x1,
                                  y1);
        //then
        //bug in wayland java bindings, we have to use an argument captor to compare Fixed object equality.
        ArgumentCaptor<Fixed> fixedArgumentCaptor = ArgumentCaptor.forClass(Fixed.class);
        final List<Fixed> values = fixedArgumentCaptor.getAllValues();

        verify(wlPointerResource0,
               times(1)).enter(eq(serial0),
                               eq(wlSurfaceResource0),
                               fixedArgumentCaptor.capture(),
                               fixedArgumentCaptor.capture());

        assertThat(values.get(0)
                         .asInt()).isEqualTo(relX1);
        assertThat(values.get(1)
                         .asInt()).isEqualTo(relY1);

        verify(wlPointerResource0,
               times(1)).motion(eq(time),
                                fixedArgumentCaptor.capture(),
                                fixedArgumentCaptor.capture());
        assertThat(values.get(2)
                         .asInt()).isEqualTo(relX1);
        assertThat(values.get(3)
                         .asInt()).isEqualTo(relY1);
    }

    /**
     * button is pressed, cursor moves from no surface into surface, button is released.
     */
    @Test
    public void testButtonNoGrabMotion() throws Exception {
        //given
        final Set<WlPointerResource> pointerResources = new HashSet<>();
        final WlPointerResource wlPointerResource0 = mock(WlPointerResource.class);
        pointerResources.add(wlPointerResource0);
        final int time = 112358;
        final int x0 = 20;
        final int y0 = 30;
        final PointImmutable pos0 = new Point(x0,
                                              y0);

        final int x1 = 100;
        final int y1 = 200;
        final PointImmutable pos1 = new Point(x1,
                                              y1);

        final int button0 = 1;

        final Client client0 = mock(Client.class);

        final WlSurfaceResource wlSurfaceResource0 = mock(WlSurfaceResource.class);

        final LinkedList<WlSurfaceResource> surfaceResources = new LinkedList<>();
        surfaceResources.add(wlSurfaceResource0);
        when(this.compositor.getSurfacesStack()).thenReturn(surfaceResources);

        final WlSurface wlSurface0 = mock(WlSurface.class);
        when(wlSurfaceResource0.getImplementation()).thenReturn(wlSurface0);

        final Surface surface0 = mock(Surface.class);
        when(wlSurface0.getSurface()).thenReturn(surface0);

        WlRegionResource wlRegionResource0 = mock(WlRegionResource.class);
        when(surface0.getInputRegion()).thenReturn(Optional.of(wlRegionResource0));

        final WlRegion wlRegion0 = mock(WlRegion.class);
        when(wlRegionResource0.getImplementation()).thenReturn(wlRegion0);

        final Region region0 = mock(Region.class);
        when(wlRegion0.getRegion()).thenReturn(region0);

        when(wlSurfaceResource0.getClient()).thenReturn(client0);

        when(wlPointerResource0.getClient()).thenReturn(client0);

        final PointImmutable surfacePos0 = new Point(100,
                                                     100);
        final int relX0 = x0 - 100;
        final int relY0 = y0 - 100;
        final PointImmutable relPos0 = new Point(relX0,
                                                 relY0);
        when(surface0.relativeCoordinate(pos0)).thenReturn(relPos0);
        when(region0.contains(relPos0)).thenReturn(false);

        when(surface0.getPosition()).thenReturn(surfacePos0);

        final int relX1 = x1 - 100;
        final int relY1 = y1 - 100;
        final PointImmutable relPos1 = new Point(relX1,
                                                 relY1);
        when(surface0.relativeCoordinate(pos1)).thenReturn(relPos1);
        when(region0.contains(relPos1)).thenReturn(true);

        final int serial0 = 90879;
        final int serial1 = 90880;
        final int serial2 = 90881;
        when(this.display.nextSerial()).thenReturn(serial0,
                                                   serial1,
                                                   serial2);
        //when
        this.pointerDevice.motion(pointerResources,
                                  time,
                                  x0,
                                  y0);
        this.pointerDevice.button(pointerResources,
                                  time,
                                  button0,
                                  WlPointerButtonState.PRESSED);
        this.pointerDevice.motion(pointerResources,
                                  time,
                                  x1,
                                  y1);
        this.pointerDevice.button(pointerResources,
                                  time,
                                  button0,
                                  WlPointerButtonState.RELEASED);
        //then
        //bug in wayland java bindings, we have to use an argument captor to compare Fixed object equality.
        ArgumentCaptor<Fixed> fixedArgumentCaptor = ArgumentCaptor.forClass(Fixed.class);
        final List<Fixed> values = fixedArgumentCaptor.getAllValues();

        verify(wlPointerResource0,
               times(1)).enter(eq(serial0),
                               eq(wlSurfaceResource0),
                               fixedArgumentCaptor.capture(),
                               fixedArgumentCaptor.capture());

        assertThat(values.get(0)
                         .asInt()).isEqualTo(relX1);
        assertThat(values.get(1)
                         .asInt()).isEqualTo(relY1);

        verify(wlPointerResource0,
               times(1)).motion(eq(time),
                                fixedArgumentCaptor.capture(),
                                fixedArgumentCaptor.capture());
        assertThat(values.get(2)
                         .asInt()).isEqualTo(relX1);
        assertThat(values.get(3)
                         .asInt()).isEqualTo(relY1);

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
        final Set<WlPointerResource> pointerResources = new HashSet<>();
        final WlPointerResource wlPointerResource0 = mock(WlPointerResource.class);
        pointerResources.add(wlPointerResource0);
        final int time = 112358;
        final int x0 = 20;
        final int y0 = 30;
        final PointImmutable pos0 = new Point(x0,
                                              y0);

        final int button0 = 1;

        final Client client0 = mock(Client.class);

        final WlSurfaceResource wlSurfaceResource0 = mock(WlSurfaceResource.class);

        final LinkedList<WlSurfaceResource> surfaceResources = new LinkedList<>();
        surfaceResources.add(wlSurfaceResource0);
        when(this.compositor.getSurfacesStack()).thenReturn(surfaceResources);

        final WlSurface wlSurface0 = mock(WlSurface.class);
        when(wlSurfaceResource0.getImplementation()).thenReturn(wlSurface0);

        final Surface surface0 = mock(Surface.class);
        when(wlSurface0.getSurface()).thenReturn(surface0);

        WlRegionResource wlRegionResource0 = mock(WlRegionResource.class);
        when(surface0.getInputRegion()).thenReturn(Optional.of(wlRegionResource0));

        final WlRegion wlRegion0 = mock(WlRegion.class);
        when(wlRegionResource0.getImplementation()).thenReturn(wlRegion0);

        final Region region0 = mock(Region.class);
        when(wlRegion0.getRegion()).thenReturn(region0);

        when(wlSurfaceResource0.getClient()).thenReturn(client0);

        when(wlPointerResource0.getClient()).thenReturn(client0);

        final int relX0 = 50;
        final int relY0 = 100;
        final PointImmutable relPos0 = new Point(relX0,
                                                 relY0);
        when(surface0.relativeCoordinate(pos0)).thenReturn(relPos0);
        when(region0.contains(relPos0)).thenReturn(true);

        final PointImmutable surfacePos0 = new Point(x0 - relX0,
                                                     y0 - relY0);
        when(surface0.getPosition()).thenReturn(surfacePos0);

        final int serial0 = 90879;
        final int serial1 = 90880;
        final int serial2 = 90881;
        when(this.display.nextSerial()).thenReturn(serial0,
                                                   serial1,
                                                   serial2);
        //when
        this.pointerDevice.motion(pointerResources,
                                  time,
                                  x0,
                                  y0);
        this.pointerDevice.button(pointerResources,
                                  time,
                                  button0,
                                  WlPointerButtonState.PRESSED);
        this.pointerDevice.button(pointerResources,
                                  time,
                                  button0,
                                  WlPointerButtonState.RELEASED);
        //then
        //bug in wayland java bindings, we have to use an argument captor to compare Fixed object equality.
        ArgumentCaptor<Fixed> fixedArgumentCaptor = ArgumentCaptor.forClass(Fixed.class);
        final List<Fixed> values = fixedArgumentCaptor.getAllValues();

        verify(wlPointerResource0,
               times(1)).enter(eq(serial0),
                               eq(wlSurfaceResource0),
                               fixedArgumentCaptor.capture(),
                               fixedArgumentCaptor.capture());

        assertThat(values.get(0)
                         .asInt()).isEqualTo(relX0);
        assertThat(values.get(1)
                         .asInt()).isEqualTo(relY0);

        verify(wlPointerResource0,
               times(1)).motion(eq(time),
                                fixedArgumentCaptor.capture(),
                                fixedArgumentCaptor.capture());
        assertThat(values.get(2)
                         .asInt()).isEqualTo(relX0);
        assertThat(values.get(3)
                         .asInt()).isEqualTo(relY0);

        verify(wlPointerResource0,
               times(1)).button(serial1,
                                time,
                                button0,
                                WlPointerButtonState.PRESSED.getValue());

        verify(wlPointerResource0,
               times(1)).button(serial2,
                                time,
                                button0,
                                WlPointerButtonState.RELEASED.getValue());
    }

    @Test
    public void testIsButtonPressed() throws Exception {
        //given
        final Set<WlPointerResource> pointerResources = new HashSet<>();
        final WlPointerResource wlPointerResource0 = mock(WlPointerResource.class);
        pointerResources.add(wlPointerResource0);
        final int time = 112358;
        final int x0 = 20;
        final int y0 = 30;
        final PointImmutable pos0 = new Point(x0,
                                              y0);

        final int button0 = 1;

        final Client client0 = mock(Client.class);

        final WlSurfaceResource wlSurfaceResource0 = mock(WlSurfaceResource.class);

        final LinkedList<WlSurfaceResource> surfaceResources = new LinkedList<>();
        surfaceResources.add(wlSurfaceResource0);
        when(this.compositor.getSurfacesStack()).thenReturn(surfaceResources);

        final WlSurface wlSurface0 = mock(WlSurface.class);
        when(wlSurfaceResource0.getImplementation()).thenReturn(wlSurface0);

        final Surface surface0 = mock(Surface.class);
        when(wlSurface0.getSurface()).thenReturn(surface0);

        WlRegionResource wlRegionResource0 = mock(WlRegionResource.class);
        when(surface0.getInputRegion()).thenReturn(Optional.of(wlRegionResource0));

        final WlRegion wlRegion0 = mock(WlRegion.class);
        when(wlRegionResource0.getImplementation()).thenReturn(wlRegion0);

        final Region region0 = mock(Region.class);
        when(wlRegion0.getRegion()).thenReturn(region0);

        when(wlSurfaceResource0.getClient()).thenReturn(client0);

        when(wlPointerResource0.getClient()).thenReturn(client0);

        final int relX0 = 50;
        final int relY0 = 100;
        final PointImmutable relPos0 = new Point(relX0,
                                                 relY0);
        when(surface0.relativeCoordinate(pos0)).thenReturn(relPos0);
        when(region0.contains(relPos0)).thenReturn(true);

        final PointImmutable surfacePos0 = new Point(x0 - relX0,
                                                     y0 - relY0);
        when(surface0.getPosition()).thenReturn(surfacePos0);

        final int serial0 = 90879;
        final int serial1 = 90880;
        final int serial2 = 90881;
        when(this.display.nextSerial()).thenReturn(serial0,
                                                   serial1,
                                                   serial2);
        //when
        this.pointerDevice.motion(pointerResources,
                                  time,
                                  x0,
                                  y0);
        this.pointerDevice.button(pointerResources,
                                  time,
                                  button0,
                                  WlPointerButtonState.PRESSED);
        //then
        assertThat(this.pointerDevice.isButtonPressed(button0)).isTrue();
    }

    @Test
    public void testMove() throws Exception {
        //given

    }

    @Test
    public void testResize() throws Exception {
        //TODO
    }
}