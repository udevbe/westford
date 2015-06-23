package org.westmalle.wayland.x11;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.westmalle.wayland.nativ.Libxcb;
import org.westmalle.wayland.output.Compositor;
import org.westmalle.wayland.output.JobExecutor;
import org.westmalle.wayland.output.Keyboard;
import org.westmalle.wayland.output.KeyboardFactory;
import org.westmalle.wayland.output.Output;
import org.westmalle.wayland.output.PointerDevice;
import org.westmalle.wayland.output.PointerDeviceFactory;
import org.westmalle.wayland.protocol.WlKeyboard;
import org.westmalle.wayland.protocol.WlKeyboardFactory;
import org.westmalle.wayland.protocol.WlOutput;
import org.westmalle.wayland.protocol.WlPointer;
import org.westmalle.wayland.protocol.WlPointerFactory;
import org.westmalle.wayland.protocol.WlSeat;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({WlPointerFactory.class,
                 WlKeyboardFactory.class,
                 PointerDeviceFactory.class,
                 KeyboardFactory.class})
public class X11SeatFactoryTest {

    @Mock
    private Libxcb libxcb;
    @Mock
    private JobExecutor jobExecutor;
    @Mock
    private WlPointerFactory wlPointerFactory;
    @Mock
    private WlKeyboardFactory wlKeyboardFactory;
    @Mock
    private PointerDeviceFactory pointerDeviceFactory;
    @Mock
    private KeyboardFactory keyboardFactory;
    @InjectMocks
    private X11SeatFactory x11SeatFactory;

    @Test
    public void testCreate() throws Exception {
        //given
        final WlOutput wlOutput = mock(WlOutput.class);
        final Output output = mock(Output.class);
        final X11Output x11Output = mock(X11Output.class);
        final X11EventBus x11EventBus = mock(X11EventBus.class);
        final WlSeat wlSeat = mock(WlSeat.class);
        final Compositor compositor = mock(Compositor.class);
        final PointerDevice pointerDevice = mock(PointerDevice.class);
        final WlPointer wlPointer = mock(WlPointer.class);
        final Keyboard keyboard = mock(Keyboard.class);
        final WlKeyboard wlKeyboard = mock(WlKeyboard.class);

        when(wlOutput.getOutput()).thenReturn(output);
        when(output.getImplementation()).thenReturn(x11Output);
        when(x11Output.getX11EventBus()).thenReturn(x11EventBus);
        when(this.pointerDeviceFactory.create(compositor)).thenReturn(pointerDevice);
        when(this.wlPointerFactory.create(pointerDevice)).thenReturn(wlPointer);
        when(this.keyboardFactory.create()).thenReturn(keyboard);
        when(this.wlKeyboardFactory.create(keyboard)).thenReturn(wlKeyboard);

        //when
        final X11Seat x11Seat = this.x11SeatFactory.create(wlOutput,
                                                           wlSeat,
                                                           compositor);
        //then
        assertThat(x11Seat).isNotNull();
        verify(wlSeat).setWlPointer(wlPointer);
        verify(wlSeat).setWlKeyboard(wlKeyboard);
        verify(x11EventBus).register(x11Seat);
    }
}