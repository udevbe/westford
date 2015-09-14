package org.westmalle.wayland.x11;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.westmalle.wayland.core.Seat;
import org.westmalle.wayland.nativ.libxcb.Libxcb;
import org.westmalle.wayland.nativ.libxcb.xcb_generic_event_t;
import org.westmalle.wayland.protocol.WlSeat;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyShort;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class X11InputEventListenerTest {

    @Mock
    private WlSeat                wlSeat;
    @InjectMocks
    private X11InputEventListener x11InputEventListener;

    @Test
    public void testHandleXcbKeyPressEvent() throws Exception {
        //given
        final X11Seat x11Seat = mock(X11Seat.class);
        final Seat    seat    = mock(Seat.class);
        when(seat.getPlatformImplementation()).thenReturn(x11Seat);
        when(this.wlSeat.getSeat()).thenReturn(seat);
        final xcb_generic_event_t event = mock(xcb_generic_event_t.class);
        event.response_type = Libxcb.XCB_KEY_PRESS;
        //when
        this.x11InputEventListener.handle(event);
        //then
        verify(x11Seat).deliverKey(eq(this.wlSeat),
                                   anyShort(),
                                   eq(true));
    }

    @Test
    public void testHandleXcbButtonPressEvent() throws Exception {
        //given
        final X11Seat x11Seat = mock(X11Seat.class);
        final Seat    seat    = mock(Seat.class);
        when(seat.getPlatformImplementation()).thenReturn(x11Seat);
        when(this.wlSeat.getSeat()).thenReturn(seat);
        final xcb_generic_event_t event = mock(xcb_generic_event_t.class);
        event.response_type = Libxcb.XCB_BUTTON_PRESS;
        //when
        this.x11InputEventListener.handle(event);
        //then
        verify(x11Seat).deliverButton(eq(this.wlSeat),
                                      anyInt(),
                                      anyShort(),
                                      eq(true));
    }

    @Test
    public void testHandleXcbKeyReleaseEvent() throws Exception {
        //given
        final X11Seat x11Seat = mock(X11Seat.class);
        final Seat    seat    = mock(Seat.class);
        when(seat.getPlatformImplementation()).thenReturn(x11Seat);
        when(this.wlSeat.getSeat()).thenReturn(seat);
        final xcb_generic_event_t event = mock(xcb_generic_event_t.class);
        event.response_type = Libxcb.XCB_KEY_RELEASE;
        //when
        this.x11InputEventListener.handle(event);
        //then
        verify(x11Seat).deliverKey(eq(this.wlSeat),
                                   anyShort(),
                                   eq(false));
    }

    @Test
    public void testHandleXcbButtonReleaseEvent() throws Exception {
        //given
        final X11Seat x11Seat = mock(X11Seat.class);
        final Seat    seat    = mock(Seat.class);
        when(seat.getPlatformImplementation()).thenReturn(x11Seat);
        when(this.wlSeat.getSeat()).thenReturn(seat);
        final xcb_generic_event_t event = mock(xcb_generic_event_t.class);
        event.response_type = Libxcb.XCB_BUTTON_RELEASE;
        //when
        this.x11InputEventListener.handle(event);
        //then
        verify(x11Seat).deliverButton(eq(this.wlSeat),
                                      anyInt(),
                                      anyShort(),
                                      eq(false));
    }

    @Test
    public void testHandleXcbMotionNotifyEvent() throws Exception {
        //given
        final X11Seat x11Seat = mock(X11Seat.class);
        final Seat    seat    = mock(Seat.class);
        when(seat.getPlatformImplementation()).thenReturn(x11Seat);
        when(this.wlSeat.getSeat()).thenReturn(seat);
        final xcb_generic_event_t event = mock(xcb_generic_event_t.class);
        event.response_type = Libxcb.XCB_MOTION_NOTIFY;
        //when
        this.x11InputEventListener.handle(event);
        //then
        x11Seat.deliverMotion(eq(this.wlSeat),
                              anyInt(),
                              anyInt());
    }
}