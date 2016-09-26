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


import org.freedesktop.jaccall.Pointer;
import org.westmalle.nativ.glibc.Libc;
import org.westmalle.nativ.linux.vt_mode;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.logging.Logger;

import static java.lang.String.format;
import static org.westmalle.nativ.glibc.Libc.O_CLOEXEC;
import static org.westmalle.nativ.glibc.Libc.O_NOCTTY;
import static org.westmalle.nativ.glibc.Libc.O_RDWR;
import static org.westmalle.nativ.glibc.Libc.O_WRONLY;
import static org.westmalle.nativ.linux.Kd.KDGETMODE;
import static org.westmalle.nativ.linux.Kd.KDGKBMODE;
import static org.westmalle.nativ.linux.Kd.KDSETMODE;
import static org.westmalle.nativ.linux.Kd.KDSKBMODE;
import static org.westmalle.nativ.linux.Kd.KD_GRAPHICS;
import static org.westmalle.nativ.linux.Kd.KD_TEXT;
import static org.westmalle.nativ.linux.Kd.K_OFF;
import static org.westmalle.nativ.linux.Kd.K_UNICODE;
import static org.westmalle.nativ.linux.Stat.KDSKBMUTE;
import static org.westmalle.nativ.linux.Vt.VT_ACTIVATE;
import static org.westmalle.nativ.linux.Vt.VT_OPENQRY;
import static org.westmalle.nativ.linux.Vt.VT_PROCESS;
import static org.westmalle.nativ.linux.Vt.VT_SETMODE;
import static org.westmalle.nativ.linux.Vt.VT_WAITACTIVE;

public class TtyFactory {

    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    @Nonnull
    private final Libc              libc;
    @Nonnull
    private final PrivateTtyFactory privateTtyFactory;

    private final short relSig;
    private final short acqSig;

    @Inject
    TtyFactory(@Nonnull final Libc libc,
               @Nonnull final PrivateTtyFactory privateTtyFactory) {
        this.libc = libc;
        this.privateTtyFactory = privateTtyFactory;

        /*
        * SIGRTMIN is used as global VT-acquire+release signal. Note that
        * SIGRT* must be tested on runtime, as their exact values are not
        * known at compile-time. POSIX requires 32 of them to be available.
        */
        if (this.libc.SIGRTMIN() > this.libc.SIGRTMAX() ||
            this.libc.SIGRTMIN() + 1 > this.libc.SIGRTMAX()) {
            throw new RuntimeException(String.format("not enough RT signals available: %d-%d\n",
                                                     this.libc.SIGRTMIN(),
                                                     this.libc.SIGRTMAX()));
        }

        this.relSig = (short) this.libc.SIGRTMIN();
        this.acqSig = (short) (this.libc.SIGRTMIN() + 1);
    }

    public Tty create(final int ttyFd) {
        return this.privateTtyFactory.create(ttyFd,
                                             K_UNICODE,
                                             this.relSig,
                                             this.acqSig);
    }

    public Tty create() {
        //TODO tty from config?
        final Pointer<Integer> ttynr = Pointer.nref(0);

        final int tty0 = this.libc.open(Pointer.nref("/dev/tty0").address,
                                        O_WRONLY | O_CLOEXEC);

        if (-1 == tty0) {
            throw new RuntimeException("Could not open /dev/tty0 : " + this.libc.getStrError());
        }

        if (-1 == this.libc.ioctl(tty0,
                                  VT_OPENQRY,
                                  ttynr.address) || -1 == ttynr.dref()) {
            throw new RuntimeException("Failed to query for open vt: " + this.libc.getStrError());
        }
        final Integer vt = ttynr.dref();
        final int ttyFd = this.libc.open(Pointer.nref(format("/dev/tty%d",
                                                             vt)).address,
                                         O_RDWR | O_NOCTTY);
        this.libc.close(tty0);

        if (-1 == ttyFd) {
            throw new RuntimeException(format("Failed to open /dev/tty%d : " + this.libc.getStrError(),
                                              vt));
        }

        LOGGER.info(format("Using /dev/tty%d",
                           vt));

        final Pointer<Integer> kd_mode = Pointer.nref(0);
        if (-1 == this.libc.ioctl(ttyFd,
                                  KDGETMODE,
                                  kd_mode.address)) {
            throw new RuntimeException("Failed to get VT mode: " + this.libc.getStrError());
        }
        final int oldKdMode = kd_mode.dref();

        if (oldKdMode != KD_TEXT) {
            throw new RuntimeException("Already in graphics mode, is another display server running?");
        }

        if (-1 == this.libc.ioctl(ttyFd,
                                  VT_ACTIVATE,
                                  vt)) {
            throw new Error(String.format("ioctl[VT_ACTIVATE, %d] failed: %s",
                                          vt,
                                          this.libc.getStrError()));
        }

        if (-1 == this.libc.ioctl(ttyFd,
                                  VT_WAITACTIVE,
                                  vt)) {
            throw new Error(String.format("ioctl[VT_WAITACTIVE, %d] failed: %s",
                                          vt,
                                          this.libc.getStrError()));
        }

        final Pointer<Integer> kb_mode = Pointer.nref(0);
        if (-1 == this.libc.ioctl(ttyFd,
                                  KDGKBMODE,
                                  kb_mode.address)) {
            throw new RuntimeException("Failed to read keyboard mode: " + this.libc.getStrError());
        }
        final int oldKbMode = kb_mode.dref();

        if (-1 == this.libc.ioctl(ttyFd,
                                  KDSKBMUTE,
                                  1) &&
            -1 == this.libc.ioctl(ttyFd,
                                  KDSKBMODE,
                                  K_OFF)) {
            throw new RuntimeException("Failed to set K_OFF keyboard mode: " + this.libc.getStrError());
        }

        if (-1 == this.libc.ioctl(ttyFd,
                                  KDSETMODE,
                                  KD_GRAPHICS)) {
            throw new RuntimeException("Failed to set KD_GRAPHICS mode on tty: " + this.libc.getStrError());
        }

        final vt_mode mode = new vt_mode();
        mode.mode(VT_PROCESS);
        mode.relsig(this.relSig);
        mode.acqsig(this.acqSig);
        mode.waitv((byte) 0);
        mode.frsig((byte) 0);
        if (-1 == this.libc.ioctl(ttyFd,
                                  VT_SETMODE,
                                  Pointer.ref(mode).address)) {
            throw new RuntimeException("Failed to take control of vt handling: " + this.libc.getStrError());
        }


        return this.privateTtyFactory.create(ttyFd,
                                             oldKbMode,
                                             this.relSig,
                                             this.acqSig);
    }
}
