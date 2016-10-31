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
package org.westmalle.compositor.x11;

import org.freedesktop.jaccall.Pointer;
import org.freedesktop.wayland.shared.WlSeatCapability;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;
import org.westmalle.Signal;
import org.westmalle.Slot;
import org.westmalle.compositor.core.KeyboardDevice;
import org.westmalle.compositor.core.KeyboardDeviceFactory;
import org.westmalle.compositor.core.Output;
import org.westmalle.compositor.core.PointerDevice;
import org.westmalle.compositor.core.Seat;
import org.westmalle.compositor.core.Xkb;
import org.westmalle.compositor.protocol.WlKeyboard;
import org.westmalle.compositor.protocol.WlKeyboardFactory;
import org.westmalle.compositor.protocol.WlOutput;
import org.westmalle.compositor.protocol.WlPointer;
import org.westmalle.compositor.protocol.WlPointerFactory;
import org.westmalle.compositor.protocol.WlSeat;
import org.westmalle.compositor.protocol.WlSeatFactory;
import org.westmalle.nativ.libxcb.xcb_generic_event_t;

import java.util.EnumSet;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
public class X11SeatFactoryTest {

    @Mock
    private X11Platform                  x11Platform;
    @Mock
    private X11XkbFactory                x11XkbFactory;
    @Mock
    private X11InputEventListenerFactory x11InputEventListenerFactory;
    @Mock
    private WlSeatFactory                wlSeatFactory;
    @Mock
    private WlPointerFactory             wlPointerFactory;
    @Mock
    private WlKeyboardFactory            wlKeyboardFactory;
    @Mock
    private KeyboardDeviceFactory        keyboardDeviceFactory;
    @Mock
    private PrivateX11SeatFactory        privateX11SeatFactory;

    @InjectMocks
    private X11SeatFactory x11SeatFactory;

    @Test
    public void testCreate() throws Exception {
        //given
        final WlOutput wlOutput = mock(WlOutput.class);
        final Output   output   = mock(Output.class);

        final String                                                                   keymapString          = "mock keymap";
        final X11InputEventListener                                                    x11InputEventListener = mock(X11InputEventListener.class);
        final Seat                                                                     seat                  = mock(Seat.class);
        final WlSeat                                                                   wlSeat                = mock(WlSeat.class);
        final X11Seat                                                                  x11Seat               = mock(X11Seat.class);
        final long                                                                     xcbConnection         = 8433272;
        final X11EventBus                                                              x11EventBus           = mock(X11EventBus.class);
        final Signal<Pointer<xcb_generic_event_t>, Slot<Pointer<xcb_generic_event_t>>> xEventSignal          = mock(Signal.class);
        final Xkb                                                                      xkb                   = mock(Xkb.class);
        final PointerDevice                                                            pointerDevice         = mock(PointerDevice.class);
        final WlPointer                                                                wlPointer             = mock(WlPointer.class);
        final KeyboardDevice                                                           keyboardDevice        = mock(KeyboardDevice.class);
        final WlKeyboard                                                               wlKeyboard            = mock(WlKeyboard.class);

        when(wlOutput.getOutput()).thenReturn(output);
        when(this.x11Platform.getX11EventBus()).thenReturn(x11EventBus);
        when(x11EventBus.getXEventSignal()).thenReturn(xEventSignal);
        when(this.x11Platform.getXcbConnection()).thenReturn(xcbConnection);

        when(xkb.getKeymapString()).thenReturn(keymapString);
        when(keyboardDevice.getXkb()).thenReturn(xkb);
        when(wlSeat.getSeat()).thenReturn(seat);
        when(wlSeat.getWlPointer()).thenReturn(wlPointer);
        when(wlSeat.getWlKeyboard()).thenReturn(wlKeyboard);
        when(wlPointer.getPointerDevice()).thenReturn(pointerDevice);
        when(wlKeyboard.getKeyboardDevice()).thenReturn(keyboardDevice);
        when(this.wlSeatFactory.create(wlPointer,
                                       wlKeyboard)).thenReturn(wlSeat);
        when(this.privateX11SeatFactory.create(wlSeat)).thenReturn(x11Seat);
        when(this.x11InputEventListenerFactory.create(x11Seat)).thenReturn(x11InputEventListener);
        when(this.x11XkbFactory.create(xcbConnection)).thenReturn(xkb);
        when(this.keyboardDeviceFactory.create(xkb)).thenReturn(keyboardDevice);
        when(this.wlKeyboardFactory.create(keyboardDevice)).thenReturn(wlKeyboard);
        when(this.wlPointerFactory.create()).thenReturn(wlPointer);

        //when
        this.x11SeatFactory.create();

        //then
        verify(seat).setCapabilities(EnumSet.of(WlSeatCapability.KEYBOARD,
                                                WlSeatCapability.POINTER));
        verify(keyboardDevice).updateKeymap();
    }
}