/*
 * Westford Wayland Compositor.
 * Copyright (C) 2016  Erik De Rijcke
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.westford.compositor.input;

import org.freedesktop.jaccall.Pointer;
import org.freedesktop.jaccall.Ptr;
import org.westford.compositor.core.KeyboardDevice;
import org.westford.compositor.core.KeyboardDeviceFactory;
import org.westford.compositor.protocol.WlKeyboardFactory;
import org.westford.compositor.protocol.WlPointerFactory;
import org.westford.compositor.protocol.WlSeat;
import org.westford.compositor.protocol.WlSeatFactory;
import org.westford.launch.LifeCycleSignals;
import org.westford.nativ.glibc.Libc;
import org.westford.nativ.libinput.Libinput;
import org.westford.nativ.libinput.Pointerclose_restricted;
import org.westford.nativ.libinput.libinput_interface;
import org.westford.nativ.libudev.Libudev;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import static org.freedesktop.jaccall.Pointer.malloc;
import static org.westford.nativ.libinput.Pointeropen_restricted.nref;

public class LibinputSeatFactory {

    @Nonnull
    private final WlSeatFactory              wlSeatFactory;
    @Nonnull
    private final WlKeyboardFactory          wlKeyboardFactory;
    @Nonnull
    private final WlPointerFactory           wlPointerFactory;
    @Nonnull
    private final PrivateLibinputSeatFactory privateLibinputSeatFactory;
    @Nonnull
    private final KeyboardDeviceFactory      keyboardDeviceFactory;
    @Nonnull
    private final LibinputXkbFactory         libinputXkbFactory;
    @Nonnull
    private final Libinput                   libinput;
    @Nonnull
    private final Libudev                    libudev;
    @Nonnull
    private final Libc                       libc;
    @Nonnull
    private final LifeCycleSignals           lifeCycleSignals;

    @Inject
    LibinputSeatFactory(@Nonnull final WlSeatFactory wlSeatFactory,
                        @Nonnull final WlKeyboardFactory wlKeyboardFactory,
                        @Nonnull final WlPointerFactory wlPointerFactory,
                        @Nonnull final PrivateLibinputSeatFactory privateLibinputSeatFactory,
                        @Nonnull final KeyboardDeviceFactory keyboardDeviceFactory,
                        @Nonnull final LibinputXkbFactory libinputXkbFactory,
                        @Nonnull final Libinput libinput,
                        @Nonnull final Libudev libudev,
                        @Nonnull final Libc libc,
                        @Nonnull final LifeCycleSignals lifeCycleSignals) {
        this.wlSeatFactory = wlSeatFactory;
        this.wlKeyboardFactory = wlKeyboardFactory;
        this.wlPointerFactory = wlPointerFactory;
        this.privateLibinputSeatFactory = privateLibinputSeatFactory;
        this.keyboardDeviceFactory = keyboardDeviceFactory;
        this.libinputXkbFactory = libinputXkbFactory;
        this.libinput = libinput;
        this.libudev = libudev;
        this.libc = libc;
        this.lifeCycleSignals = lifeCycleSignals;
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

        final WlSeat wlSeat = this.wlSeatFactory.create(this.wlPointerFactory.create(),
                                                        this.wlKeyboardFactory.create(keyboardDevice));

        final LibinputSeat libinputSeat = this.privateLibinputSeatFactory.create(createUdevContext(seatId),
                                                                                 wlSeat);
        libinputSeat.enableInput();

        this.lifeCycleSignals.getActivateSignal()
                             .connect(event -> libinputSeat.enableInput());
        this.lifeCycleSignals.getDeactivateSignal()
                             .connect(event -> libinputSeat.disableInput());

        return wlSeat;
    }

    private long createUdevContext(final String seatId) {
        final long udev = this.libudev.udev_new();
        if (udev == 0L) {
            throw new RuntimeException("Failed to initialize udev");
        }

        final Pointer<libinput_interface> interface_ = malloc(libinput_interface.SIZE,
                                                              libinput_interface.class);
        interface_.dref()
                  .open_restricted(nref(this::openRestricted));
        interface_.dref()
                  .close_restricted(Pointerclose_restricted.nref(this::closeRestricted));

        final long libinput = this.libinput.libinput_udev_create_context(interface_.address,
                                                                         0,
                                                                         udev);

        if (this.libinput.libinput_udev_assign_seat(libinput,
                                                    Pointer.nref(seatId).address) != 0) {
            this.libinput.libinput_unref(libinput);
            this.libudev.udev_unref(udev);

            throw new RuntimeException(String.format("Failed to set seat=%s",
                                                     seatId));
        }

        return libinput;
    }

    private int openRestricted(@Ptr(String.class) final long path,
                               final int flags,
                               @Ptr(Void.class) final long user_data) {
        final int fd = this.libc.open(path,
                                      flags);

        return fd < 0 ? -this.libc.getErrno() : fd;
    }

    private void closeRestricted(final int fd,
                                 @Ptr(Void.class) final long user_data) {
        this.libc.close(fd);
    }
}
