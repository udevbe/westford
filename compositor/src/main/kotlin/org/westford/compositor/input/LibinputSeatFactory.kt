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
package org.westford.compositor.input

import org.freedesktop.jaccall.Pointer
import org.freedesktop.jaccall.Pointer.malloc
import org.freedesktop.jaccall.Pointer.nref
import org.freedesktop.jaccall.Ptr
import org.westford.compositor.protocol.WlPointerFactory
import org.westford.compositor.protocol.WlSeat
import org.westford.launch.LifeCycleSignals
import org.westford.nativ.glibc.Libc
import org.westford.nativ.libinput.Libinput
import org.westford.nativ.libinput.Pointerclose_restricted
import org.westford.nativ.libinput.libinput_interface
import org.westford.nativ.libudev.Libudev
import javax.inject.Inject

class LibinputSeatFactory @Inject internal constructor(private val wlSeatFactory: WlSeatFactory,
                                                       private val wlKeyboardFactory: WlKeyboardFactory,
                                                       private val wlPointerFactory: WlPointerFactory,
                                                       private val privateLibinputSeatFactory: PrivateLibinputSeatFactory,
                                                       private val keyboardDeviceFactory: KeyboardDeviceFactory,
                                                       private val libinputXkbFactory: LibinputXkbFactory,
                                                       private val libinput: Libinput,
                                                       private val libudev: Libudev,
                                                       private val libc: Libc,
                                                       private val lifeCycleSignals: LifeCycleSignals) {

    fun create(seatId: String,
               keyboardRule: String,
               keyboardModel: String,
               keyboardLayout: String,
               keyboardVariant: String,
               keyboardOptions: String): WlSeat {
        val keyboardDevice = this.keyboardDeviceFactory.create(this.libinputXkbFactory.create(keyboardRule,
                                                                                              keyboardModel,
                                                                                              keyboardLayout,
                                                                                              keyboardVariant,
                                                                                              keyboardOptions))
        keyboardDevice.updateKeymap()

        val wlSeat = this.wlSeatFactory.create(this.wlPointerFactory.create(),
                                               this.wlKeyboardFactory.create(keyboardDevice))

        val libinputSeat = this.privateLibinputSeatFactory.create(createUdevContext(seatId),
                                                                  wlSeat)
        libinputSeat.enableInput()

        this.lifeCycleSignals.activateSignal.connect {
            libinputSeat.enableInput()
        }
        this.lifeCycleSignals.deactivateSignal.connect {
            libinputSeat.disableInput()
        }

        return wlSeat
    }

    private fun createUdevContext(seatId: String): Long {
        val udev = this.libudev.udev_new()
        if (udev == 0L) {
            throw RuntimeException("Failed to initialize udev")
        }

        val interface_ = malloc<libinput_interface>(libinput_interface.SIZE,
                                                    libinput_interface::class.java)
        interface_.dref().open_restricted(nref(???({ path, flags, user_data ->
            this.openRestricted(path,
                                flags,
                                user_data)
        })))
        interface_.dref().close_restricted(Pointerclose_restricted.nref(???({ fd, user_data ->
            this.closeRestricted(fd,
                                 user_data)
        })))

        val libinput = this.libinput.libinput_udev_create_context(interface_.address,
                                                                  0,
                                                                  udev)

        if (this.libinput.libinput_udev_assign_seat(libinput,
                                                    Pointer.nref(seatId).address) != 0) {
            this.libinput.libinput_unref(libinput)
            this.libudev.udev_unref(udev)

            throw RuntimeException(String.format("Failed to set seat=%s",
                                                 seatId))
        }

        return libinput
    }

    private fun openRestricted(@Ptr(String::class) path: Long,
                               flags: Int,
                               @Ptr(Void::class) user_data: Long): Int {
        val fd = this.libc.open(path,
                                flags)

        return if (fd < 0) -this.libc.errno else fd
    }

    private fun closeRestricted(fd: Int,
                                @Ptr(Void::class) user_data: Long) {
        this.libc.close(fd)
    }
}
