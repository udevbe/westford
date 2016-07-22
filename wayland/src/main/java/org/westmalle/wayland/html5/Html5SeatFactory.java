package org.westmalle.wayland.html5;


import com.google.auto.factory.Provided;
import org.westmalle.wayland.core.KeyboardDevice;
import org.westmalle.wayland.core.KeyboardDeviceFactory;
import org.westmalle.wayland.input.LibinputXkbFactory;
import org.westmalle.wayland.protocol.WlKeyboardFactory;
import org.westmalle.wayland.protocol.WlSeat;
import org.westmalle.wayland.protocol.WlSeatFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;

public class Html5SeatFactory {

    @Nonnull
    private final PrivateHtml5SeatFactory privateHtml5SeatFactory;
    @Nonnull
    private final WlSeatFactory           wlSeatFactory;
    @Nonnull
    private final WlKeyboardFactory       wlKeyboardFactory;
    @Nonnull
    private final KeyboardDeviceFactory   keyboardDeviceFactory;
    @Nonnull
    private final LibinputXkbFactory      libinputXkbFactory;

    @Inject
    Html5SeatFactory(@Nonnull final PrivateHtml5SeatFactory privateHtml5SeatFactory,
                     @Nonnull final WlSeatFactory wlSeatFactory,
                     @Nonnull final WlKeyboardFactory wlKeyboardFactory,
                     @Nonnull final KeyboardDeviceFactory keyboardDeviceFactory,
                     @Nonnull final LibinputXkbFactory libinputXkbFactory) {
        this.privateHtml5SeatFactory = privateHtml5SeatFactory;
        this.wlSeatFactory = wlSeatFactory;
        this.wlKeyboardFactory = wlKeyboardFactory;
        this.keyboardDeviceFactory = keyboardDeviceFactory;
        this.libinputXkbFactory = libinputXkbFactory;
    }

    public Html5Seat create() {
        final String keyboardRule    = "";
        final String keyboardModel   = "";
        final String keyboardLayout  = "";
        final String keyboardVariant = "";
        final String keyboardOptions = "";

        final KeyboardDevice keyboardDevice = this.keyboardDeviceFactory.create(this.libinputXkbFactory.create(keyboardRule,
                                                                                                               keyboardModel,
                                                                                                               keyboardLayout,
                                                                                                               keyboardVariant,
                                                                                                               keyboardOptions));
        keyboardDevice.updateKeymap();
        final WlSeat wlSeat = this.wlSeatFactory.create(this.wlKeyboardFactory.create(keyboardDevice));
        return this.privateHtml5SeatFactory.create(wlSeat);
    }
}
