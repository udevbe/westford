package org.westmalle.wayland.x11;

import org.freedesktop.jaccall.Pointer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.westmalle.wayland.core.Seat;
import org.westmalle.wayland.nativ.libxcb.Libxcb;
import org.westmalle.wayland.nativ.libxcb.xcb_button_press_event_t;
import org.westmalle.wayland.nativ.libxcb.xcb_generic_event_t;
import org.westmalle.wayland.nativ.libxcb.xcb_key_press_event_t;
import org.westmalle.wayland.nativ.libxcb.xcb_motion_notify_event_t;
import org.westmalle.wayland.protocol.WlSeat;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyShort;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class X11InputEventCodesEventListenerTest {

    @Mock
    private X11XkbFactory         x11XkbFactory;
    @Mock
    private X11Seat               x11Seat;
    @InjectMocks
    private X11InputEventListener x11InputEventListener;

    @Test
    public void testHandleXcbKeyPressEvent() throws Exception {
        //given
        final WlSeat wlSeat = mock(WlSeat.class);
        when(this.x11Seat.getWlSeat()).thenReturn(wlSeat);
        final Seat seat = mock(Seat.class);
        when(wlSeat.getSeat()).thenReturn(seat);
        final Pointer<xcb_key_press_event_t> event = Pointer.malloc(xcb_key_press_event_t.SIZE,
                                                                    xcb_key_press_event_t.class);
        event.dref()
             .response_type((byte) Libxcb.XCB_KEY_PRESS);
        //when
        this.x11InputEventListener.handle(event.castp(xcb_generic_event_t.class));
        //then
        verify(this.x11Seat).deliverKey(anyInt(),
                                        anyShort(),
                                        eq(true));
    }

    @Test
    public void testHandleXcbButtonPressEvent() throws Exception {
        //given
        final WlSeat wlSeat = mock(WlSeat.class);
        when(this.x11Seat.getWlSeat()).thenReturn(wlSeat);
        final Seat seat = mock(Seat.class);
        when(wlSeat.getSeat()).thenReturn(seat);
        final Pointer<xcb_button_press_event_t> event = Pointer.malloc(xcb_button_press_event_t.SIZE,
                                                                       xcb_button_press_event_t.class);
        event.dref()
             .response_type((byte) Libxcb.XCB_BUTTON_PRESS);
        //when
        this.x11InputEventListener.handle(event.castp(xcb_generic_event_t.class));
        //then
        verify(this.x11Seat).deliverButton(eq(event.dref()
                                                   .event()),
                                           anyInt(),
                                           anyShort(),
                                           eq(true));
    }

    @Test
    public void testHandleXcbKeyReleaseEvent() throws Exception {
        //given
        final WlSeat wlSeat = mock(WlSeat.class);
        when(this.x11Seat.getWlSeat()).thenReturn(wlSeat);
        final Seat seat = mock(Seat.class);
        when(wlSeat.getSeat()).thenReturn(seat);
        final Pointer<xcb_key_press_event_t> event = Pointer.malloc(xcb_key_press_event_t.SIZE,
                                                                    xcb_key_press_event_t.class);
        event.dref()
             .response_type((byte) Libxcb.XCB_KEY_RELEASE);
        //when
        this.x11InputEventListener.handle(event.castp(xcb_generic_event_t.class));
        //then
        verify(this.x11Seat).deliverKey(anyInt(),
                                        anyShort(),
                                        eq(false));
    }

    @Test
    public void testHandleXcbButtonReleaseEvent() throws Exception {
        //given
        final WlSeat wlSeat = mock(WlSeat.class);
        when(this.x11Seat.getWlSeat()).thenReturn(wlSeat);
        final Seat seat = mock(Seat.class);
        when(wlSeat.getSeat()).thenReturn(seat);
        final Pointer<xcb_button_press_event_t> event = Pointer.malloc(xcb_button_press_event_t.SIZE,
                                                                       xcb_button_press_event_t.class);
        event.dref()
             .response_type((byte) Libxcb.XCB_BUTTON_RELEASE);
        //when
        this.x11InputEventListener.handle(event.castp(xcb_generic_event_t.class));
        //then
        verify(this.x11Seat).deliverButton(eq(event.dref()
                                                   .event()),
                                           anyInt(),
                                           anyShort(),
                                           eq(false));
    }

    @Test
    public void testHandleXcbMotionNotifyEvent() throws Exception {
        //given
        final WlSeat wlSeat = mock(WlSeat.class);
        when(this.x11Seat.getWlSeat()).thenReturn(wlSeat);
        final Seat seat = mock(Seat.class);
        when(wlSeat.getSeat()).thenReturn(seat);
        final Pointer<xcb_motion_notify_event_t> event = Pointer.malloc(xcb_motion_notify_event_t.SIZE,
                                                                        xcb_motion_notify_event_t.class);
        event.dref()
             .response_type((byte) Libxcb.XCB_MOTION_NOTIFY);
        //when
        this.x11InputEventListener.handle(event.castp(xcb_generic_event_t.class));
        //then
        this.x11Seat.deliverMotion(anyInt(),
                                   anyInt(),
                                   anyInt(),
                                   anyInt());
    }

    //TODO add tests for all events
}