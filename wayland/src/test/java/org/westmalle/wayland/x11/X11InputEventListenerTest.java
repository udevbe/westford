package org.westmalle.wayland.x11;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.westmalle.wayland.nativ.libxcb.xcb_button_press_event_t;
import org.westmalle.wayland.nativ.libxcb.xcb_button_release_event_t;
import org.westmalle.wayland.nativ.libxcb.xcb_key_press_event_t;
import org.westmalle.wayland.nativ.libxcb.xcb_key_release_event_t;
import org.westmalle.wayland.nativ.libxcb.xcb_motion_notify_event_t;
import org.westmalle.wayland.protocol.WlSeat;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class X11InputEventListenerTest {

    @Mock
    private WlSeat                wlSeat;
    @Mock
    private X11Seat               x11Seat;
    @InjectMocks
    private X11InputEventListener x11InputEventListener;

    @Test
    public void testHandleXcbKeyPressEvent() throws Exception {
        //given
        final xcb_key_press_event_t event = mock(xcb_key_press_event_t.class);
        //when
        this.x11InputEventListener.handle(event);
        //then
        verify(this.x11Seat).deliverKey(this.wlSeat,
                                        event.detail,
                                        true);
    }

    @Test
    public void testHandleXcbButtonPressEvent() throws Exception {
        //given
        final xcb_button_press_event_t event = mock(xcb_button_press_event_t.class);
        //when
        this.x11InputEventListener.handle(event);
        //then
        verify(this.x11Seat).deliverButton(this.wlSeat,
                                           event.time,
                                           event.detail,
                                           true);
    }

    @Test
    public void testHandleXcbKeyReleaseEvent() throws Exception {
        //given
        final xcb_key_release_event_t event = mock(xcb_key_release_event_t.class);
        //when
        this.x11InputEventListener.handle(event);
        //then
        verify(this.x11Seat).deliverKey(this.wlSeat,
                                        event.detail,
                                        false);
    }

    @Test
    public void testHandleXcbButtonReleaseEvent() throws Exception {
        //given
        final xcb_button_release_event_t event = mock(xcb_button_release_event_t.class);
        //when
        this.x11InputEventListener.handle(event);
        //then
        verify(this.x11Seat).deliverButton(this.wlSeat,
                                           event.time,
                                           event.detail,
                                           false);
    }

    @Test
    public void testHandleXcbMotionNotifyEvent() throws Exception {
        //given
        final xcb_motion_notify_event_t event = mock(xcb_motion_notify_event_t.class);
        //when
        this.x11InputEventListener.handle(event);
        //then
        this.x11Seat.deliverMotion(this.wlSeat,
                                   event.event_x,
                                   event.event_y);
    }
}