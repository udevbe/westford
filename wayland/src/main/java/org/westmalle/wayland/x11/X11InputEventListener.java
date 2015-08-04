package org.westmalle.wayland.x11;


import com.google.auto.factory.AutoFactory;
import com.google.common.eventbus.Subscribe;
import org.westmalle.wayland.nativ.libxcb.xcb_button_press_event_t;
import org.westmalle.wayland.nativ.libxcb.xcb_button_release_event_t;
import org.westmalle.wayland.nativ.libxcb.xcb_key_press_event_t;
import org.westmalle.wayland.nativ.libxcb.xcb_key_release_event_t;
import org.westmalle.wayland.nativ.libxcb.xcb_motion_notify_event_t;
import org.westmalle.wayland.protocol.WlSeat;

import javax.annotation.Nonnull;

@AutoFactory
public class X11InputEventListener {

    @Nonnull
    private final WlSeat  wlSeat;
    @Nonnull
    private final X11Seat x11Seat;

    X11InputEventListener(@Nonnull final WlSeat wlSeat,
                          @Nonnull final X11Seat x11Seat) {
        this.wlSeat = wlSeat;
        this.x11Seat = x11Seat;
    }

    @Subscribe
    public void handle(final xcb_key_press_event_t event) {
        this.x11Seat.deliverKey(this.wlSeat,
                                event.detail,
                                true);
    }

    @Subscribe
    public void handle(final xcb_button_press_event_t event) {
        this.x11Seat.deliverButton(this.wlSeat,
                                   event.time,
                                   event.detail,
                                   true);
    }

    @Subscribe
    public void handle(final xcb_key_release_event_t event) {
        this.x11Seat.deliverKey(this.wlSeat,
                                event.detail,
                                false);
    }

    @Subscribe
    public void handle(final xcb_button_release_event_t event) {
        this.x11Seat.deliverButton(this.wlSeat,
                                   event.time,
                                   event.detail,
                                   false);
    }

    @Subscribe
    public void handle(final xcb_motion_notify_event_t event) {
        this.x11Seat.deliverMotion(this.wlSeat,
                                   event.event_x,
                                   event.event_y);
    }

    //TODO keymap change event
}
