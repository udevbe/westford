//Copyright 2016 Erik De Rijcke
//
//Licensed under the Apache License,Version2.0(the"License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
//http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing,software
//distributed under the License is distributed on an"AS IS"BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
package org.westmalle.wayland.input;

import org.freedesktop.jaccall.Pointer;
import org.freedesktop.jaccall.Ptr;
import org.westmalle.wayland.core.KeyboardDevice;
import org.westmalle.wayland.core.KeyboardDeviceFactory;
import org.westmalle.wayland.nativ.glibc.Libc;
import org.westmalle.wayland.nativ.libinput.Libinput;
import org.westmalle.wayland.nativ.libinput.Pointerclose_restricted;
import org.westmalle.wayland.nativ.libinput.libinput_interface;
import org.westmalle.wayland.nativ.libudev.Libudev;
import org.westmalle.wayland.protocol.WlKeyboardFactory;
import org.westmalle.wayland.protocol.WlSeat;
import org.westmalle.wayland.protocol.WlSeatFactory;
import org.westmalle.wayland.tty.Tty;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import static org.freedesktop.jaccall.Pointer.malloc;
import static org.westmalle.wayland.nativ.libinput.Pointeropen_restricted.nref;

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
    @Nonnull
    private final Tty                        tty;
    @Nonnull
    private final Libinput                   libinput;
    @Nonnull
    private final Libudev                    libudev;
    @Nonnull
    private final Libc                       libc;

    @Inject
    LibinputSeatFactory(@Nonnull final WlSeatFactory wlSeatFactory,
                        @Nonnull final WlKeyboardFactory wlKeyboardFactory,
                        @Nonnull final PrivateLibinputSeatFactory privateLibinputSeatFactory,
                        @Nonnull final KeyboardDeviceFactory keyboardDeviceFactory,
                        @Nonnull final LibinputXkbFactory libinputXkbFactory,
                        @Nonnull final Tty tty,
                        @Nonnull final Libinput libinput,
                        @Nonnull final Libudev libudev,
                        @Nonnull final Libc libc) {
        this.wlSeatFactory = wlSeatFactory;
        this.wlKeyboardFactory = wlKeyboardFactory;
        this.privateLibinputSeatFactory = privateLibinputSeatFactory;
        this.keyboardDeviceFactory = keyboardDeviceFactory;
        this.libinputXkbFactory = libinputXkbFactory;
        this.tty = tty;
        this.libinput = libinput;
        this.libudev = libudev;
        this.libc = libc;
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

        final LibinputSeat libinputSeat = this.privateLibinputSeatFactory.create(createUdevContext(seatId),
                                                                                 wlSeat);
        libinputSeat.enableInput();

        this.tty.getVtEnterSignal()
                .connect(event -> libinputSeat.enableInput());
        this.tty.getVtLeaveSignal()
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
