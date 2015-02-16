package org.westmalle.wayland.output;

import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.WlPointerResource;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.freedesktop.wayland.shared.WlPointerButtonState;
import org.freedesktop.wayland.util.Fixed;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.media.nativewindow.util.Point;
import javax.media.nativewindow.util.PointImmutable;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

    @Test
    public void testGrabNewFocusMotionTwoButtons() throws Exception {
        //given
        final Set<WlPointerResource> pointerResources = new HashSet<>();
        final WlPointerResource wlPointerResource0 = mock(WlPointerResource.class);
        final WlPointerResource wlPointerResource1 = mock(WlPointerResource.class);
        pointerResources.add(wlPointerResource0);
        pointerResources.add(wlPointerResource1);
        final int time = 112358;
        final int x0 = 20;
        final int y0 = 30;

        final int x1 = 100;
        final int y1 = 200;

        final int button0 = 1;
        final int button1 = 2;

        final Scene scene = mock(Scene.class);
        when(this.compositor.getScene()).thenReturn(scene);

        final Client client0 = mock(Client.class);
        final Client client1 = mock(Client.class);

        final WlSurfaceResource wlSurfaceResource0 = mock(WlSurfaceResource.class);
        final WlSurfaceResource wlSurfaceResource1 = mock(WlSurfaceResource.class);

        when(wlSurfaceResource0.getClient()).thenReturn(client0);
        when(wlSurfaceResource1.getClient()).thenReturn(client1);

        when(wlPointerResource0.getClient()).thenReturn(client0);
        when(wlPointerResource1.getClient()).thenReturn(client1);

        when(scene.findSurfaceAtCoordinate(anyInt(),
                                           anyInt())).thenReturn(Optional.<WlSurfaceResource>empty());
        when(scene.findSurfaceAtCoordinate(x0,
                                           y0)).thenReturn(Optional.of(wlSurfaceResource0));
        when(scene.findSurfaceAtCoordinate(x1,
                                           y1)).thenReturn(Optional.of(wlSurfaceResource1));

        final int relX0 = 50;
        final int relY0 = 100;
        final PointImmutable pointImmutable0 = new Point(relX0,
                                                         relY0);
        when(scene.relativeCoordinate(wlSurfaceResource0,
                                      x1,
                                      y1)).thenReturn(pointImmutable0);

        final int relX1 = 0;
        final int relY1 = 100;
        final PointImmutable pointImmutable1 = new Point(relX1,
                                                         relY1);
        when(scene.relativeCoordinate(wlSurfaceResource0,
                                      x1,
                                      y1)).thenReturn(pointImmutable1);

        final int serial = 90879;
        when(this.display.nextSerial()).thenReturn(serial);
        //when
        this.pointerDevice.motion(pointerResources,
                                  time,
                                  x0,
                                  y0);
        this.pointerDevice.button(pointerResources,
                                  time,
                                  button,
                                  WlPointerButtonState.PRESSED);
        this.pointerDevice.motion(pointerResources,
                                  time,
                                  x1,
                                  y1);
        //then
        verify(wlPointerResource0,
               times(1)).button(serial,
                                time,
                                button,
                                WlPointerButtonState.PRESSED.getValue());

        ArgumentCaptor<Fixed> fixedArgumentCaptor = ArgumentCaptor.forClass(Fixed.class);
        verify(wlPointerResource0,
               times(1)).motion(eq(time),
                                fixedArgumentCaptor.capture(),
                                fixedArgumentCaptor.capture());
        //bug in wayland java bindings, we have to use an argument captor to compare Fixed object equality.
        final List<Fixed> values = fixedArgumentCaptor.getAllValues();
        assertThat(values.get(0)
                         .asInt()).isEqualTo(relX0);
        assertThat(values.get(1)
                         .asInt()).isEqualTo(relY0);
    }

    @Test
    public void testNoGrabMotion() throws Exception {

    }

    @Test
    public void testNoFocusMotion() throws Exception {

    }

    @Test
    public void testNewFocusMotion() throws Exception {

    }

    @Test
    public void testButton() throws Exception {

    }

    @Test
    public void testIsButtonPressed() throws Exception {

    }

    @Test
    public void testMove() throws Exception {

    }

    @Test
    public void testDoMotion() throws Exception {

    }

    @Test
    public void testGetGrabSerial() throws Exception {

    }
}