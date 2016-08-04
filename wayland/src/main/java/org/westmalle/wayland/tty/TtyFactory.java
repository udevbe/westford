package org.westmalle.wayland.tty;


import org.freedesktop.jaccall.Pointer;
import org.freedesktop.wayland.server.Display;
import org.westmalle.wayland.nativ.glibc.Libc;
import org.westmalle.wayland.nativ.linux.vt_mode;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.logging.Logger;

import static java.lang.String.format;
import static org.westmalle.wayland.nativ.glibc.Libc.O_CLOEXEC;
import static org.westmalle.wayland.nativ.glibc.Libc.O_NOCTTY;
import static org.westmalle.wayland.nativ.glibc.Libc.O_RDWR;
import static org.westmalle.wayland.nativ.glibc.Libc.O_WRONLY;
import static org.westmalle.wayland.nativ.linux.Kd.KDGETMODE;
import static org.westmalle.wayland.nativ.linux.Kd.KDGKBMODE;
import static org.westmalle.wayland.nativ.linux.Kd.KDSETMODE;
import static org.westmalle.wayland.nativ.linux.Kd.KDSKBMODE;
import static org.westmalle.wayland.nativ.linux.Kd.KD_GRAPHICS;
import static org.westmalle.wayland.nativ.linux.Kd.KD_TEXT;
import static org.westmalle.wayland.nativ.linux.Kd.K_OFF;
import static org.westmalle.wayland.nativ.linux.Stat.KDSKBMUTE;
import static org.westmalle.wayland.nativ.linux.Vt.VT_ACTIVATE;
import static org.westmalle.wayland.nativ.linux.Vt.VT_OPENQRY;
import static org.westmalle.wayland.nativ.linux.Vt.VT_PROCESS;
import static org.westmalle.wayland.nativ.linux.Vt.VT_SETMODE;
import static org.westmalle.wayland.nativ.linux.Vt.VT_WAITACTIVE;

public class TtyFactory {

    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    @Nonnull
    private final Libc              libc;
    @Nonnull
    private final Display           display;
    @Nonnull
    private final PrivateTtyFactory privateTtyFactory;

    @Inject
    TtyFactory(@Nonnull final Libc libc,
               @Nonnull final Display display,
               @Nonnull final PrivateTtyFactory privateTtyFactory) {
        this.libc = libc;
        this.display = display;
        this.privateTtyFactory = privateTtyFactory;
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


    public Tty create() {
        //TODO tty from config

        //FIXME we need to check if our sigproc mask is set to blocked for the signals we're interested in receiving. This must be done by the process that calls us.

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

        this.libc.ioctl(ttyFd,
                        VT_ACTIVATE,
                        vt);
        this.libc.ioctl(ttyFd,
                        VT_WAITACTIVE,
                        vt);

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
        if (this.libc.SIGRTMIN() + 5 > this.libc.SIGRTMAX()) {
            throw new RuntimeException(String.format("not enough RT signals available: %d-%d\n",
                                                     this.libc.SIGRTMIN() + 5,
                                                     this.libc.SIGRTMAX()));
        }

        final vt_mode mode = new vt_mode();
        mode.mode(VT_PROCESS);
        mode.relsig((byte) (this.libc.SIGRTMIN()));
        mode.acqsig((byte) (this.libc.SIGRTMIN()));
        mode.waitv((byte) 0);
        mode.frsig((byte) 0);
        if (this.libc.ioctl(ttyFd,
                            VT_SETMODE,
                            Pointer.ref(mode).address) < 0) {
            throw new RuntimeException("failed to take control of vt handling");
        }

        final Tty tty = this.privateTtyFactory.create(ttyFd,
                                                      vt,
                                                      oldKbMode);

        this.display.getEventLoop()
                    .addSignal(this.libc.SIGRTMIN(),
                               tty::vtHandler);

        return tty;
    }
}
