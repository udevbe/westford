/*
 * Westmalle Wayland Compositor.
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
package org.westmalle.tty;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import org.freedesktop.jaccall.Pointer;
import org.westmalle.Signal;
import org.westmalle.Slot;
import org.westmalle.nativ.glibc.Libc;
import org.westmalle.nativ.linux.vt_mode;

import java.util.logging.Logger;

import static org.westmalle.nativ.linux.Kd.KDSETMODE;
import static org.westmalle.nativ.linux.Kd.KDSKBMODE;
import static org.westmalle.nativ.linux.Kd.KD_TEXT;
import static org.westmalle.nativ.linux.Stat.KDSKBMUTE;
import static org.westmalle.nativ.linux.Vt.VT_ACKACQ;
import static org.westmalle.nativ.linux.Vt.VT_ACTIVATE;
import static org.westmalle.nativ.linux.Vt.VT_AUTO;
import static org.westmalle.nativ.linux.Vt.VT_RELDISP;
import static org.westmalle.nativ.linux.Vt.VT_SETMODE;

@AutoFactory(className = "PrivateTtyFactory",
             allowSubclasses = true)
public class Tty implements AutoCloseable {

    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    private final Libc libc;
    private final int  ttyFd;

    private final int   oldKbMode;
    private final short relSig;
    private final short acqSig;

    private final Signal<VtEnter, Slot<VtEnter>> vtEnterSignal = new Signal<>();
    private final Signal<VtLeave, Slot<VtLeave>> vtLeaveSignal = new Signal<>();

    private boolean vtActive = true;

    Tty(@Provided final Libc libc,
        final int ttyFd,
        final int oldKbMode,
        final short relSig,
        final short acqSig) {

        this.libc = libc;
        this.ttyFd = ttyFd;
        this.oldKbMode = oldKbMode;
        this.relSig = relSig;
        this.acqSig = acqSig;
    }

    public void activate(final int vt) {
        if (!this.vtActive) {
            if (this.libc.ioctl(this.ttyFd,
                                VT_ACTIVATE,
                                vt) < 0) {
                throw new RuntimeException("failed to switch to new vt.");
            }
        }
    }

    public void handleVtLeave() {

        this.vtActive = false;
        this.vtLeaveSignal.emit(VtLeave.create());

        if (-1 == this.libc.ioctl(this.ttyFd,
                                  VT_RELDISP,
                                  1)) {
            throw new Error(String.format("ioctl[VT_RELDISP, 1] failed: %s",
                                          this.libc.getStrError()));
        }
    }

    public void handleVtEnter() {

        if (-1 == this.libc.ioctl(this.ttyFd,
                                  VT_RELDISP,
                                  VT_ACKACQ)) {
            throw new Error(String.format("ioctl[VT_RELDISP, VT_ACKACQ] failed: %s",
                                          this.libc.getStrError()));
        }

        this.vtActive = true;
        this.vtEnterSignal.emit(VtEnter.create());
    }

    @Override
    public void close() {
        //restore tty
        if (this.libc.ioctl(this.ttyFd,
                            KDSKBMUTE,
                            0) != 0 &&
            this.libc.ioctl(this.ttyFd,
                            KDSKBMODE,
                            this.oldKbMode) != 0) {
            LOGGER.warning("failed to restore kb mode");
        }

        if (this.libc.ioctl(this.ttyFd,
                            KDSETMODE,
                            KD_TEXT) != 0) {
            LOGGER.warning("failed to set KD_TEXT mode on tty: %m\n");
        }

        if (this.vtActive) {
            this.vtLeaveSignal.emit(VtLeave.create());
        }

        final vt_mode mode = new vt_mode();
        mode.frsig((byte) 0);
        mode.waitv((byte) 0);
        mode.acqsig((byte) 0);
        mode.relsig((byte) 0);
        mode.mode(VT_AUTO);
        if (this.libc.ioctl(this.ttyFd,
                            VT_SETMODE,
                            Pointer.ref(mode).address) < 0) {
            LOGGER.warning("could not reset vt handling\n");
        }

        //TODO switch back to old tty


        this.libc.close(this.ttyFd);
    }

    public Signal<VtEnter, Slot<VtEnter>> getVtEnterSignal() {
        return this.vtEnterSignal;
    }

    public Signal<VtLeave, Slot<VtLeave>> getVtLeaveSignal() {
        return this.vtLeaveSignal;
    }

    public short getAcqSig() {
        return this.acqSig;
    }

    public short getRelSig() {
        return this.relSig;
    }

    public int getTtyFd() {
        return this.ttyFd;
    }
}
