package org.westmalle.wayland.html5;


import com.google.auto.factory.Provided;
import org.freedesktop.wayland.shared.WlSeatCapability;
import org.westmalle.wayland.core.KeyboardDevice;
import org.westmalle.wayland.core.KeyboardDeviceFactory;
import org.westmalle.wayland.core.PointerDevice;
import org.westmalle.wayland.input.LibinputXkbFactory;
import org.westmalle.wayland.protocol.WlKeyboard;
import org.westmalle.wayland.protocol.WlKeyboardFactory;
import org.westmalle.wayland.protocol.WlSeat;
import org.westmalle.wayland.protocol.WlSeatFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.EnumSet;

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
        //TODO make this configurable either on server or on client side.
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
        //enable pointer and keyboard for wlseat as a browser always has these.
        wlSeat.getSeat()
              .setCapabilities(EnumSet.of(WlSeatCapability.KEYBOARD,
                                          WlSeatCapability.POINTER));


        //TODO make this configurable
        //setup keyboard focus tracking to follow mouse pointer
        final WlKeyboard wlKeyboard = wlSeat.getWlKeyboard();
        final PointerDevice pointerDevice = wlSeat.getWlPointer()
                                                  .getPointerDevice();
        pointerDevice.getPointerFocusSignal()
                     .connect(event -> wlKeyboard.getKeyboardDevice()
                                                 .setFocus(wlKeyboard.getResources(),
                                                           pointerDevice.getFocus()));

        return this.privateHtml5SeatFactory.create(wlSeat);
    }
}
