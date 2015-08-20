package org.westmalle.wayland.x11;


import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import com.google.common.eventbus.Subscribe;
import org.freedesktop.wayland.shared.WlKeyboardKeymapFormat;
import org.westmalle.wayland.core.KeyboardDevice;
import org.westmalle.wayland.core.Keymap;
import org.westmalle.wayland.core.Xkb;
import org.westmalle.wayland.nativ.libxcb.xcb_button_press_event_t;
import org.westmalle.wayland.nativ.libxcb.xcb_button_release_event_t;
import org.westmalle.wayland.nativ.libxcb.xcb_key_press_event_t;
import org.westmalle.wayland.nativ.libxcb.xcb_key_release_event_t;
import org.westmalle.wayland.nativ.libxcb.xcb_keymap_notify_event_t;
import org.westmalle.wayland.nativ.libxcb.xcb_motion_notify_event_t;
import org.westmalle.wayland.protocol.WlKeyboard;
import org.westmalle.wayland.protocol.WlSeat;

import javax.annotation.Nonnull;
import java.util.Optional;

@AutoFactory
public class X11InputEventListener {

    @Nonnull
    private final X11XkbFactory x11XkbFactory;
    @Nonnull
    private final WlSeat        wlSeat;

    X11InputEventListener(@Provided @Nonnull final X11XkbFactory x11XkbFactory,
                          @Nonnull final WlSeat wlSeat) {
        this.x11XkbFactory = x11XkbFactory;
        this.wlSeat = wlSeat;
    }

    @Subscribe
    public void handle(final xcb_key_press_event_t event) {
        final X11Seat x11Seat = (X11Seat) this.wlSeat.getSeat()
                                                     .getPlatformImplementation();
        x11Seat.deliverKey(this.wlSeat,
                           event.detail,
                           true);
    }

    @Subscribe
    public void handle(final xcb_button_press_event_t event) {
        final X11Seat x11Seat = (X11Seat) this.wlSeat.getSeat()
                                                     .getPlatformImplementation();
        x11Seat.deliverButton(this.wlSeat,
                              event.time,
                              event.detail,
                              true);
    }

    @Subscribe
    public void handle(final xcb_key_release_event_t event) {
        final X11Seat x11Seat = (X11Seat) this.wlSeat.getSeat()
                                                     .getPlatformImplementation();
        x11Seat.deliverKey(this.wlSeat,
                           event.detail,
                           false);
    }

    @Subscribe
    public void handle(final xcb_button_release_event_t event) {
        final X11Seat x11Seat = (X11Seat) this.wlSeat.getSeat()
                                                     .getPlatformImplementation();
        x11Seat.deliverButton(this.wlSeat,
                              event.time,
                              event.detail,
                              false);
    }

    @Subscribe
    public void handle(final xcb_motion_notify_event_t event) {
        final X11Seat x11Seat = (X11Seat) this.wlSeat.getSeat()
                                                     .getPlatformImplementation();
        x11Seat.deliverMotion(this.wlSeat,
                              event.event_x,
                              event.event_y);
    }

    @Subscribe
    public void handle(final xcb_keymap_notify_event_t event) {
        final X11Seat x11Seat = (X11Seat) this.wlSeat.getSeat()
                                                     .getPlatformImplementation();
        final WlKeyboard     wlKeyboard     = this.wlSeat.getWlKeyboard();
        final KeyboardDevice keyboardDevice = wlKeyboard.getKeyboardDevice();
        final Xkb xkb = this.x11XkbFactory.create(x11Seat.getX11Output()
                                                         .getXcbConnection());
        keyboardDevice.setXkb(xkb);
        keyboardDevice.setKeymap(Optional.of(Keymap.create(WlKeyboardKeymapFormat.XKB_V1,
                                                           xkb.getKeymapString())));
        keyboardDevice.emitKeymap(wlKeyboard.getResources());
    }
}
