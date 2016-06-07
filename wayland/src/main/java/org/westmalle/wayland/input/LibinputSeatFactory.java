package org.westmalle.wayland.input;

import org.westmalle.wayland.core.KeyboardDevice;
import org.westmalle.wayland.core.KeyboardDeviceFactory;
import org.westmalle.wayland.protocol.WlKeyboardFactory;
import org.westmalle.wayland.protocol.WlSeat;
import org.westmalle.wayland.protocol.WlSeatFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;

public class LibinputSeatFactory {

    @Nonnull
    private final WlSeatFactory              wlSeatFactory;
    @Nonnull
    private final WlKeyboardFactory          wlKeyboardFactory;
    @Nonnull
    private final PrivateLibinputSeatFactory privateLibinputSeatFactory;
    @Nonnull
    private final KeyboardDeviceFactory      keyboardDeviceFactory;
    @Nonnull
    private final LibinputXkbFactory         libinputXkbFactory;

    @Inject
    LibinputSeatFactory(@Nonnull final WlSeatFactory wlSeatFactory,
                        @Nonnull final WlKeyboardFactory wlKeyboardFactory,
                        @Nonnull final PrivateLibinputSeatFactory privateLibinputSeatFactory,
                        @Nonnull final KeyboardDeviceFactory keyboardDeviceFactory,
                        @Nonnull final LibinputXkbFactory libinputXkbFactory) {
        this.wlSeatFactory = wlSeatFactory;
        this.wlKeyboardFactory = wlKeyboardFactory;
        this.privateLibinputSeatFactory = privateLibinputSeatFactory;
        this.keyboardDeviceFactory = keyboardDeviceFactory;
        this.libinputXkbFactory = libinputXkbFactory;
    }

    public WlSeat create(@Nonnull final String seatId,
                         @Nonnull final String keyboardRule,
                         @Nonnull final String keyboardModel,
                         @Nonnull final String keyboardLayout,
                         @Nonnull final String keyboardVariant,
                         @Nonnull final String keyboardOptions) {
        final KeyboardDevice keyboardDevice = this.keyboardDeviceFactory.create(this.libinputXkbFactory.create(keyboardRule,
                                                                                                               keyboardModel,
                                                                                                               keyboardLayout,
                                                                                                               keyboardVariant,
                                                                                                               keyboardOptions));
        keyboardDevice.updateKeymap();


        final WlSeat wlSeat = this.wlSeatFactory.create(this.wlKeyboardFactory.create(keyboardDevice));

        final LibinputSeat libinputSeat = this.privateLibinputSeatFactory.create(wlSeat);
        libinputSeat.open(seatId);

        return wlSeat;
    }
}
