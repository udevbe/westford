package org.westmalle.wayland.tty;


import org.freedesktop.jaccall.Pointer;
import org.westmalle.wayland.nativ.libc.Libc;
import org.westmalle.wayland.nativ.linux.stat;
import org.westmalle.wayland.nativ.linux.vt_mode;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import static org.westmalle.wayland.nativ.libc.Libc.O_CLOEXEC;
import static org.westmalle.wayland.nativ.libc.Libc.O_NOCTTY;
import static org.westmalle.wayland.nativ.libc.Libc.O_RDWR;
import static org.westmalle.wayland.nativ.libc.Libc.O_WRONLY;
import static org.westmalle.wayland.nativ.linux.Kd.KDGKBMODE;
import static org.westmalle.wayland.nativ.linux.Kd.KDSETMODE;
import static org.westmalle.wayland.nativ.linux.Kd.KDSKBMODE;
import static org.westmalle.wayland.nativ.linux.Kd.KD_GRAPHICS;
import static org.westmalle.wayland.nativ.linux.Kd.K_OFF;
import static org.westmalle.wayland.nativ.linux.Major.TTY_MAJOR;
import static org.westmalle.wayland.nativ.linux.Signal.SIGUSR1;
import static org.westmalle.wayland.nativ.linux.Signal.SIGUSR2;
import static org.westmalle.wayland.nativ.linux.Stat.KDSKBMUTE;
import static org.westmalle.wayland.nativ.linux.Vt.VT_OPENQRY;
import static org.westmalle.wayland.nativ.linux.Vt.VT_PROCESS;
import static org.westmalle.wayland.nativ.linux.Vt.VT_SETMODE;

public class TtyFactory {

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

        final int ttyFd = initTty();

        return this.privateTtyFactory.create();
    }

    private int initTty() {

        final int tty0 = this.libc.open(Pointer.nref("/dev/tty0").address,
                                        O_WRONLY | O_CLOEXEC);

        if (tty0 < 0) {
            throw new RuntimeException("could not open tty0");
        }

        final Pointer<Integer> ttynr = Pointer.nref(0);
        if (this.libc.ioctl(tty0,
                            VT_OPENQRY,
                            ttynr.address) < 0 || ttynr.dref() == -1) {
            throw new RuntimeException("failed to find non-opened console");
        }

        final int ttyFd = this.libc.open(Pointer.nref("/dev/tty" + ttynr.dref()).address,
                                         O_RDWR | O_NOCTTY);
        this.libc.close(tty0);

        if (ttyFd < 0) {
            throw new RuntimeException("failed to open tty");
        }

        final stat buf = new stat();
        if (this.libc.fstat(ttyFd,
                            Pointer.ref(buf).address) == -1 ||
            this.libc.major(buf.st_rdev()) != TTY_MAJOR ||
            this.libc.minor(buf.st_rdev()) == 0) {
            throw new RuntimeException("westmalle must be run from a virtual terminal");
        }

        final Pointer<Integer> kbModeP = Pointer.nref(0);
        if (this.libc.ioctl(ttyFd,
                            KDGKBMODE,
                            kbModeP.address) != 0) {
            throw new RuntimeException("failed to get current keyboard mode");
        }
        //TODO do we need this?
        final int kbMode = kbModeP.dref();

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

        final vt_mode mode = new vt_mode();
        mode.mode(VT_PROCESS);
        mode.relsig(SIGUSR1);
        mode.acqsig(SIGUSR2);
        if (this.libc.ioctl(ttyFd,
                            VT_SETMODE,
                            Pointer.ref(mode).address) < 0) {
            throw new RuntimeException("failed to take control of vt handling");
        }

        return ttyFd;
    }
}
