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
package org.westford.tty


import org.freedesktop.jaccall.Pointer
import org.westford.nativ.glibc.Libc
import org.westford.nativ.linux.Kd
import org.westford.nativ.linux.Stat
import javax.inject.Inject
import java.util.logging.Logger

import java.lang.String.format
import org.westford.nativ.linux.Vt.VT_ACTIVATE
import org.westford.nativ.linux.Vt.VT_OPENQRY
import org.westford.nativ.linux.Vt.VT_WAITACTIVE

class TtyFactory @Inject
internal constructor(private val libc: Libc,
                     private val privateTtyFactory: PrivateTtyFactory) {

    fun create(ttyFd: Int): Tty {
        return this.privateTtyFactory.create(ttyFd,
                Kd.K_UNICODE)
    }

    fun create(): Tty {
        //TODO tty from config?
        val ttynr = Pointer.nref(0)

        val tty0 = this.libc.open(Pointer.nref("/dev/tty0").address,
                Libc.O_WRONLY or Libc.O_CLOEXEC)

        if (-1 == tty0) {
            throw RuntimeException("Could not open /dev/tty0 : " + this.libc.strError)
        }

        if (-1 == this.libc.ioctl(tty0,
                VT_OPENQRY.toLong(),
                ttynr.address) || -1 == ttynr.dref()) {
            throw RuntimeException("Failed to query for open vt: " + this.libc.strError)
        }
        val vt = ttynr.dref()
        val ttyFd = this.libc.open(Pointer.nref(format("/dev/tty%d",
                vt)).address,
                Libc.O_RDWR or Libc.O_NOCTTY)
        this.libc.close(tty0)

        if (-1 == ttyFd) {
            throw RuntimeException(format("Failed to open /dev/tty%d : " + this.libc.strError,
                    vt))
        }

        LOGGER.info(format("Using /dev/tty%d",
                vt))

        val kd_mode = Pointer.nref(0)
        if (-1 == this.libc.ioctl(ttyFd,
                Kd.KDGETMODE.toLong(),
                kd_mode.address)) {
            throw RuntimeException("Failed to get VT mode: " + this.libc.strError)
        }
        val oldKdMode = kd_mode.dref()

        if (oldKdMode != Kd.KD_TEXT.toInt()) {
            throw RuntimeException("Already in graphics mode, is another display server running?")
        }

        if (-1 == this.libc.ioctl(ttyFd,
                VT_ACTIVATE.toLong(),
                vt.toLong())) {
            throw Error(String.format("ioctl[VT_ACTIVATE, %d] failed: %s",
                    vt,
                    this.libc.strError))
        }

        if (-1 == this.libc.ioctl(ttyFd,
                VT_WAITACTIVE.toLong(),
                vt.toLong())) {
            throw Error(String.format("ioctl[VT_WAITACTIVE, %d] failed: %s",
                    vt,
                    this.libc.strError))
        }

        val kb_mode = Pointer.nref(0)
        if (-1 == this.libc.ioctl(ttyFd,
                Kd.KDGKBMODE.toLong(),
                kb_mode.address)) {
            throw RuntimeException("Failed to read keyboard mode: " + this.libc.strError)
        }
        val oldKbMode = kb_mode.dref()

        if (-1 == this.libc.ioctl(ttyFd,
                Stat.KDSKBMUTE.toLong(),
                1) && -1 == this.libc.ioctl(ttyFd,
                Kd.KDSKBMODE.toLong(),
                Kd.K_OFF)) {
            throw RuntimeException("Failed to set K_OFF keyboard mode: " + this.libc.strError)
        }

        if (-1 == this.libc.ioctl(ttyFd,
                Kd.KDSETMODE.toLong(),
                Kd.KD_GRAPHICS)) {
            throw RuntimeException("Failed to set KD_GRAPHICS mode on tty: " + this.libc.strError)
        }

        return this.privateTtyFactory.create(ttyFd,
                oldKbMode)
    }

    companion object {

        private val LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME)
    }
}
