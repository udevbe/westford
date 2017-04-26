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

import com.google.auto.factory.AutoFactory
import com.google.auto.factory.Provided
import org.freedesktop.jaccall.Pointer
import org.westford.Signal
import org.westford.Slot
import org.westford.nativ.glibc.Libc
import org.westford.nativ.linux.vt_mode

import java.util.logging.Logger

import org.westford.nativ.linux.Kd.KDSETMODE
import org.westford.nativ.linux.Kd.KDSKBMODE
import org.westford.nativ.linux.Kd.KD_TEXT
import org.westford.nativ.linux.Stat.KDSKBMUTE
import org.westford.nativ.linux.Vt.VT_ACKACQ
import org.westford.nativ.linux.Vt.VT_ACTIVATE
import org.westford.nativ.linux.Vt.VT_AUTO
import org.westford.nativ.linux.Vt.VT_RELDISP
import org.westford.nativ.linux.Vt.VT_SETMODE

@AutoFactory(className = "PrivateTtyFactory", allowSubclasses = true)
class Tty internal constructor(@param:Provided private val libc: Libc,
                               val ttyFd: Int,
                               private val oldKbMode: Int) : AutoCloseable {

    val vtEnterSignal = Signal<VtEnter, Slot<VtEnter>>()
    val vtLeaveSignal = Signal<VtLeave, Slot<VtLeave>>()

    private var vtActive = true

    fun activate(vt: Int) {
        LOGGER.info(String.format("Activating vt %d.",
                vt))

        if (this.libc.ioctl(this.ttyFd,
                VT_ACTIVATE.toLong(),
                vt.toLong()) < 0) {
            throw RuntimeException("failed to switch to new vt.")
        }
    }

    fun handleVtSignal(signalNumber: Int): Int {
        if (this.vtActive) {
            LOGGER.info("Leaving our vt.")

            this.vtActive = false
            this.vtLeaveSignal.emit(VtLeave.create())

            if (-1 == this.libc.ioctl(this.ttyFd,
                    VT_RELDISP.toLong(),
                    1)) {
                throw Error(String.format("ioctl[VT_RELDISP, 1] failed: %d",
                        this.libc.errno))
            }
        } else {
            LOGGER.info("Entering our vt.")

            if (-1 == this.libc.ioctl(this.ttyFd,
                    VT_RELDISP.toLong(),
                    VT_ACKACQ)) {
                throw Error(String.format("ioctl[VT_RELDISP, VT_ACKACQ] failed: %d",
                        this.libc.errno))
            }

            this.vtActive = true
            this.vtEnterSignal.emit(VtEnter.create())
        }

        return 1
    }

    override fun close() {
        //restore tty
        if (this.libc.ioctl(this.ttyFd,
                KDSKBMUTE.toLong(),
                0) != 0 && this.libc.ioctl(this.ttyFd,
                KDSKBMODE.toLong(),
                this.oldKbMode.toLong()) != 0) {
            LOGGER.warning("failed to restore kb mode")
        }

        if (this.libc.ioctl(this.ttyFd,
                KDSETMODE.toLong(),
                KD_TEXT) != 0) {
            LOGGER.warning("failed to set KD_TEXT mode on tty: %m\n")
        }

        if (this.vtActive) {
            this.vtLeaveSignal.emit(VtLeave.create())
        }

        val mode = vt_mode()
        mode.frsig(0.toByte())
        mode.waitv(0.toByte())
        mode.acqsig(0.toByte())
        mode.relsig(0.toByte())
        mode.mode(VT_AUTO)
        if (this.libc.ioctl(this.ttyFd,
                VT_SETMODE.toLong(),
                Pointer.ref(mode).address) < 0) {
            LOGGER.warning("could not reset vt handling\n")
        }

        //TODO switch back to old tty


        this.libc.close(this.ttyFd)
    }

    companion object {

        private val LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME)
    }
}
