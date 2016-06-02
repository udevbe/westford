package org.westmalle.wayland.input;


import org.westmalle.wayland.core.KeyboardDevice;
import org.westmalle.wayland.core.KeyboardDeviceFactory;
import org.westmalle.wayland.core.PointerDeviceFactory;
import org.westmalle.wayland.core.SeatFactory;
import org.westmalle.wayland.protocol.WlKeyboardFactory;
import org.westmalle.wayland.protocol.WlPointerFactory;
import org.westmalle.wayland.protocol.WlSeat;
import org.westmalle.wayland.protocol.WlSeatFactory;
import org.westmalle.wayland.protocol.WlTouchFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;

public class LibinputSeatFactory {

    @Nonnull
    private final SeatFactory                seatFactory;
    @Nonnull
    private final WlSeatFactory              wlSeatFactory;
    @Nonnull
    private final WlPointerFactory           wlPointerFactory;
    @Nonnull
    private final WlKeyboardFactory          wlKeyboardFactory;
    @Nonnull
    private final WlTouchFactory             wlTouchFactory;
    @Nonnull
    private final PrivateLibinputSeatFactory privateLibinputSeatFactory;
    @Nonnull
    private final PointerDeviceFactory       pointerDeviceFactory;
    @Nonnull
    private final KeyboardDeviceFactory      keyboardDeviceFactory;
    @Nonnull
    private final LibinputXkbFactory         libinputXkbFactory;

    @Inject
    LibinputSeatFactory(@Nonnull final SeatFactory seatFactory,
                        @Nonnull final WlSeatFactory wlSeatFactory,
                        @Nonnull final WlPointerFactory wlPointerFactory,
                        @Nonnull final WlKeyboardFactory wlKeyboardFactory,
                        @Nonnull final WlTouchFactory wlTouchFactory,
                        @Nonnull final PrivateLibinputSeatFactory privateLibinputSeatFactory,
                        @Nonnull final PointerDeviceFactory pointerDeviceFactory,
                        @Nonnull final KeyboardDeviceFactory keyboardDeviceFactory,
                        @Nonnull final LibinputXkbFactory libinputXkbFactory) {
        this.seatFactory = seatFactory;
        this.wlSeatFactory = wlSeatFactory;
        this.wlPointerFactory = wlPointerFactory;
        this.wlKeyboardFactory = wlKeyboardFactory;
        this.wlTouchFactory = wlTouchFactory;
        this.privateLibinputSeatFactory = privateLibinputSeatFactory;
        this.pointerDeviceFactory = pointerDeviceFactory;
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



        final WlSeat wlSeat = this.wlSeatFactory.create(this.seatFactory.create(),
                                                        this.wlPointerFactory.create(this.pointerDeviceFactory.create()),
                                                        this.wlKeyboardFactory.create(keyboardDevice),
                                                        this.wlTouchFactory.create());

        final LibinputSeat libinputSeat = this.privateLibinputSeatFactory.create(wlSeat);
        libinputSeat.open(seatId);

        return wlSeat;
    }
}
