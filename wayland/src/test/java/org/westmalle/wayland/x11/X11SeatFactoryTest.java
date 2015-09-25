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

import com.sun.jna.Pointer;
import org.freedesktop.wayland.shared.WlSeatCapability;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.westmalle.wayland.core.Compositor;
import org.westmalle.wayland.core.KeyboardDevice;
import org.westmalle.wayland.core.KeyboardDeviceFactory;
import org.westmalle.wayland.core.Output;
import org.westmalle.wayland.core.PointerDevice;
import org.westmalle.wayland.core.PointerDeviceFactory;
import org.westmalle.wayland.core.Seat;
import org.westmalle.wayland.core.SeatFactory;
import org.westmalle.wayland.core.Xkb;
import org.westmalle.wayland.core.events.Signal;
import org.westmalle.wayland.core.events.Slot;
import org.westmalle.wayland.nativ.libxcb.xcb_generic_event_t;
import org.westmalle.wayland.protocol.WlKeyboard;
import org.westmalle.wayland.protocol.WlKeyboardFactory;
import org.westmalle.wayland.protocol.WlOutput;
import org.westmalle.wayland.protocol.WlPointer;
import org.westmalle.wayland.protocol.WlPointerFactory;
import org.westmalle.wayland.protocol.WlSeat;
import org.westmalle.wayland.protocol.WlSeatFactory;
import org.westmalle.wayland.protocol.WlTouch;
import org.westmalle.wayland.protocol.WlTouchFactory;

import java.util.EnumSet;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({X11InputEventListenerFactory.class,
                 WlSeatFactory.class,
                 SeatFactory.class,
                 WlPointerFactory.class,
                 WlKeyboardFactory.class,
                 WlTouchFactory.class,
                 PointerDeviceFactory.class,
                 KeyboardDeviceFactory.class})
public class X11SeatFactoryTest {

    @Mock
    private PrivateX11SeatFactory        privateX11SeatFactory;
    @Mock
    private X11XkbFactory                x11XkbFactory;
    @Mock
    private X11InputEventListenerFactory x11InputEventListenerFactory;
    @Mock
    private WlSeatFactory                wlSeatFactory;
    @Mock
    private SeatFactory                  seatFactory;
    @Mock
    private WlPointerFactory             wlPointerFactory;
    @Mock
    private WlKeyboardFactory            wlKeyboardFactory;
    @Mock
    private WlTouchFactory               wlTouchFactory;
    @Mock
    private PointerDeviceFactory         pointerDeviceFactory;
    @Mock
    private KeyboardDeviceFactory        keyboardDeviceFactory;
    @InjectMocks
    private X11SeatFactory               x11SeatFactory;

    @Test
    public void testCreate() throws Exception {
        //given
        final WlOutput wlOutput = mock(WlOutput.class);
        final Output   output   = mock(Output.class);

        final String                                                 keymapString          = "mock keymap";
        final X11InputEventListener                                  x11InputEventListener = mock(X11InputEventListener.class);
        final Seat                                                   seat                  = mock(Seat.class);
        final WlSeat                                                 wlSeat                = mock(WlSeat.class);
        final X11Output                                              x11Output             = mock(X11Output.class);
        final Pointer                                                xcbConnection         = mock(Pointer.class);
        final X11EventBus                                            x11EventBus           = mock(X11EventBus.class);
        final Signal<xcb_generic_event_t, Slot<xcb_generic_event_t>> xEventSignal          = mock(Signal.class);
        final Compositor                                             compositor            = mock(Compositor.class);
        final Xkb                                                    xkb                   = mock(Xkb.class);
        final PointerDevice                                          pointerDevice         = mock(PointerDevice.class);
        final WlPointer                                              wlPointer             = mock(WlPointer.class);
        final KeyboardDevice                                         keyboardDevice        = mock(KeyboardDevice.class);
        final WlKeyboard                                             wlKeyboard            = mock(WlKeyboard.class);
        final WlTouch                                                wlTouch               = mock(WlTouch.class);

        when(wlOutput.getOutput()).thenReturn(output);
        when(output.getPlatformImplementation()).thenReturn(x11Output);
        when(x11Output.getX11EventBus()).thenReturn(x11EventBus);
        when(x11EventBus.getXEventSignal()).thenReturn(xEventSignal);
        when(x11Output.getXcbConnection()).thenReturn(xcbConnection);

        when(xkb.getKeymapString()).thenReturn(keymapString);
        when(keyboardDevice.getXkb()).thenReturn(xkb);
        when(this.wlTouchFactory.create()).thenReturn(wlTouch);
        when(this.seatFactory.create(any())).thenReturn(seat);
        when(wlSeat.getSeat()).thenReturn(seat);
        when(wlSeat.getWlPointer()).thenReturn(wlPointer);
        when(wlSeat.getWlKeyboard()).thenReturn(wlKeyboard);
        when(wlPointer.getPointerDevice()).thenReturn(pointerDevice);
        when(wlKeyboard.getKeyboardDevice()).thenReturn(keyboardDevice);
        when(this.wlSeatFactory.create(seat,
                                       wlPointer,
                                       wlKeyboard,
                                       wlTouch)).thenReturn(wlSeat);
        when(this.x11InputEventListenerFactory.create(wlSeat)).thenReturn(x11InputEventListener);
        when(this.x11XkbFactory.create(xcbConnection)).thenReturn(xkb);
        when(this.pointerDeviceFactory.create(compositor)).thenReturn(pointerDevice);
        when(this.wlPointerFactory.create(pointerDevice)).thenReturn(wlPointer);
        when(this.keyboardDeviceFactory.create(compositor,
                                               xkb)).thenReturn(keyboardDevice);
        when(this.wlKeyboardFactory.create(keyboardDevice)).thenReturn(wlKeyboard);

        //when
        this.x11SeatFactory.create(wlOutput,
                                   compositor);
        //then
        verify(seat).setCapabilities(EnumSet.of(WlSeatCapability.KEYBOARD,
                                                WlSeatCapability.POINTER));
        verify(keyboardDevice).updateKeymap();
    }
}