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
package org.westmalle.wayland.x11;

import org.freedesktop.wayland.server.WlPointerResource;
import org.freedesktop.wayland.shared.WlPointerButtonState;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.westmalle.wayland.core.PointerDevice;
import org.westmalle.wayland.nativ.libxcb.Libxcb;
import org.westmalle.wayland.protocol.WlPointer;
import org.westmalle.wayland.protocol.WlSeat;

import java.util.Collections;
import java.util.Set;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.westmalle.wayland.nativ.linux.Input.BTN_LEFT;
import static org.westmalle.wayland.nativ.linux.Input.BTN_MIDDLE;
import static org.westmalle.wayland.nativ.linux.Input.BTN_RIGHT;

@RunWith(MockitoJUnitRunner.class)
public class X11SeatTest {

    @Mock
    private Libxcb      libxcb;
    @Mock
    private X11Platform x11Platform;
    @Mock
    private WlSeat      wlSeat;

    @InjectMocks
    private X11Seat x11Seat;


    @Test
    public void testHandleButtonPressLeft() throws Exception {
        testHandleButtonPress((byte) 1,
                              BTN_LEFT);
    }

    private void testHandleButtonPress(final byte xEventDetail,
                                       final int waylandEventDetail) {
        //given
        final WlPointer wlPointer = mock(WlPointer.class);
        when(this.wlSeat.getWlPointer()).thenReturn(wlPointer);
        final PointerDevice pointerDevice = mock(PointerDevice.class);
        when(wlPointer.getPointerDevice()).thenReturn(pointerDevice);
        final WlPointerResource      wlPointerResource  = mock(WlPointerResource.class);
        final Set<WlPointerResource> wlPointerResources = Collections.singleton(wlPointerResource);
        when(wlPointer.getResources()).thenReturn(wlPointerResources);
        final int time   = 2134;
        final int window = 123;

        //when
        this.x11Seat.deliverButton(window,
                                   time,
                                   xEventDetail,
                                   true);
        //then
        verify(pointerDevice).button(wlPointerResources,
                                     time,
                                     waylandEventDetail,
                                     WlPointerButtonState.PRESSED);
    }

    @Test
    public void testHandleButtonPressMiddle() throws Exception {
        testHandleButtonPress((byte) 2,
                              BTN_MIDDLE);
    }

    @Test
    public void testHandleButtonPressRight() throws Exception {
        testHandleButtonPress((byte) 3,
                              BTN_RIGHT);
    }

    @Test
    public void testHandleButtonReleaseLeft() throws Exception {
        testHandleButtonRelease((byte) 1,
                                BTN_LEFT);
    }

    private void testHandleButtonRelease(final byte xEventDetail,
                                         final int waylandEventDetail) {
        //given
        final WlPointer wlPointer = mock(WlPointer.class);
        when(this.wlSeat.getWlPointer()).thenReturn(wlPointer);
        final PointerDevice pointerDevice = mock(PointerDevice.class);
        when(wlPointer.getPointerDevice()).thenReturn(pointerDevice);
        final WlPointerResource      wlPointerResource  = mock(WlPointerResource.class);
        final Set<WlPointerResource> wlPointerResources = Collections.singleton(wlPointerResource);
        when(wlPointer.getResources()).thenReturn(wlPointerResources);
        final int time   = 9876;
        final int window = 123;

        //when
        this.x11Seat.deliverButton(window,
                                   time,
                                   xEventDetail,
                                   false);
        //then
        verify(pointerDevice).button(wlPointerResources,
                                     time,
                                     waylandEventDetail,
                                     WlPointerButtonState.RELEASED);
    }

    @Test
    public void testHandleButtonReleaseMiddle() throws Exception {
        testHandleButtonRelease((byte) 2,
                                BTN_MIDDLE);
    }

    @Test
    public void testHandleButtonReleaseRight() throws Exception {
        testHandleButtonRelease((byte) 3,
                                BTN_RIGHT);
    }

    @Test
    public void testHandleMotion() throws Exception {
        //given
        final WlPointer wlPointer = mock(WlPointer.class);
        when(this.wlSeat.getWlPointer()).thenReturn(wlPointer);
        final PointerDevice pointerDevice = mock(PointerDevice.class);
        when(wlPointer.getPointerDevice()).thenReturn(pointerDevice);
        final WlPointerResource      wlPointerResource  = mock(WlPointerResource.class);
        final Set<WlPointerResource> wlPointerResources = Collections.singleton(wlPointerResource);
        when(wlPointer.getResources()).thenReturn(wlPointerResources);
        final int time = 2134;
        final int x    = 80;
        final int y    = -120;
        //when
        this.x11Seat.deliverMotion(12345,
                                   time,
                                   x,
                                   y);
        //then
        verify(pointerDevice).motion(wlPointerResources,
                                     time,
                                     x,
                                     y);
    }
}