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

    @Inject
    TtyFactory(@Nonnull final Libc libc,
               @Nonnull final PrivateTtyFactory privateTtyFactory) {
        this.libc = libc;
        this.privateTtyFactory = privateTtyFactory;
    }

    public Tty create() {
        //TODO tty from config
        final Pointer<Integer> ttynr = Pointer.nref(0);
        final int              ttyFd = getTtyFd(ttynr);
        final int              vt    = ttynr.dref();

        final Pointer<Integer> kd_mode = Pointer.nref(0);
        if (this.libc.ioctl(ttyFd,
                            KDGETMODE,
                            kd_mode.address) != 0) {
            throw new RuntimeException("failed to get VT mode: %m\n");
        }
        final int oldKdMode = kd_mode.dref();

        if (oldKdMode != KD_TEXT) {
            throw new RuntimeException("Already in graphics mode, is another display server running?");
        }

        if (-1 == this.libc.ioctl(ttyFd,
                                  VT_ACTIVATE,
                                  vt)) {
            throw new Error(String.format("ioctl[VT_ACTIVATE, %d] failed: %d",
                                          vt,
                                          this.libc.getErrno()));
        }

        if (-1 == this.libc.ioctl(ttyFd,
                                  VT_WAITACTIVE,
                                  vt)) {
            throw new Error(String.format("ioctl[VT_WAITACTIVE, %d] failed: %d",
                                          vt,
                                          this.libc.getErrno()));
        }

        final Pointer<Integer> kb_mode = Pointer.nref(0);
        if (this.libc.ioctl(ttyFd,
                            KDGKBMODE,
                            kb_mode.address) != 0) {
            throw new RuntimeException("failed to read keyboard mode");
        }
        final int oldKbMode = kb_mode.dref();

        if (this.libc.ioctl(ttyFd,
                            KDSKBMUTE,
                            1) != 0 &&
            this.libc.ioctl(ttyFd,
                            KDSKBMODE,
                            K_OFF) != 0) {
            throw new RuntimeException("failed to set K_OFF keyboard mode");
        }

        if (this.libc.ioctl(ttyFd,
                            KDSETMODE,
                            KD_GRAPHICS) != 0) {
            throw new RuntimeException("failed to set KD_GRAPHICS mode on tty");
        }

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

        final short relSig = (short) this.libc.SIGRTMIN();
        final short acqSig = (short) (this.libc.SIGRTMIN() + 1);

        final vt_mode mode = new vt_mode();
        mode.mode(VT_PROCESS);
        mode.relsig(relSig);
        mode.acqsig(acqSig);
        mode.waitv((byte) 0);
        mode.frsig((byte) 0);
        if (this.libc.ioctl(ttyFd,
                            VT_SETMODE,
                            Pointer.ref(mode).address) < 0) {
            throw new RuntimeException("failed to take control of vt handling");
        }

        return this.privateTtyFactory.create(ttyFd,
                                             vt,
                                             oldKbMode,
                                             relSig,
                                             acqSig);
    }

    private int getTtyFd(final Pointer<Integer> ttynr) {
        final int tty0 = this.libc.open(Pointer.nref("/dev/tty0").address,
                                        O_WRONLY | O_CLOEXEC);

        if (tty0 < 0) {
            throw new RuntimeException("Could not open /dev/tty0.");
        }

        if (this.libc.ioctl(tty0,
                            VT_OPENQRY,
                            ttynr.address) < 0 || ttynr.dref() == -1) {
            throw new RuntimeException("Failed to query for open vt.");
        }
        final Integer vt = ttynr.dref();
        final int ttyFd = this.libc.open(Pointer.nref(format("/dev/tty%d",
                                                             vt)).address,
                                         O_RDWR | O_NOCTTY);
        this.libc.close(tty0);

        if (ttyFd < 0) {
            throw new RuntimeException(format("Failed to open /dev/tty%d",
                                              vt));
        }

        LOGGER.info(format("Using /dev/tty%d",
                           vt));

        return ttyFd;
    }
}
