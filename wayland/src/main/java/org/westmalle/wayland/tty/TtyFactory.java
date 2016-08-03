package org.westmalle.wayland.tty;


import org.freedesktop.jaccall.Pointer;
import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.EventSource;
import org.westmalle.wayland.nativ.glibc.Libc;
import org.westmalle.wayland.nativ.glibc.termios;
import org.westmalle.wayland.nativ.linux.vt_mode;
import org.westmalle.wayland.nativ.linux.vt_stat;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.Optional;
import java.util.logging.Logger;

import static java.lang.String.format;
import static org.freedesktop.wayland.server.jaccall.WaylandServerCore.WL_EVENT_READABLE;
import static org.westmalle.wayland.nativ.glibc.Libc.O_CLOEXEC;
import static org.westmalle.wayland.nativ.glibc.Libc.O_NOCTTY;
import static org.westmalle.wayland.nativ.glibc.Libc.O_RDWR;
import static org.westmalle.wayland.nativ.glibc.Libc.O_WRONLY;
import static org.westmalle.wayland.nativ.linux.Kd.KDGKBMODE;
import static org.westmalle.wayland.nativ.linux.Kd.KDSETMODE;
import static org.westmalle.wayland.nativ.linux.Kd.KDSKBMODE;
import static org.westmalle.wayland.nativ.linux.Kd.KD_GRAPHICS;
import static org.westmalle.wayland.nativ.linux.Kd.KD_TEXT;
import static org.westmalle.wayland.nativ.linux.Kd.K_OFF;
import static org.westmalle.wayland.nativ.linux.Kd.K_RAW;
import static org.westmalle.wayland.nativ.linux.Signal.SIGUSR1;
import static org.westmalle.wayland.nativ.linux.Signal.SIGUSR2;
import static org.westmalle.wayland.nativ.linux.TermBits.OCRNL;
import static org.westmalle.wayland.nativ.linux.TermBits.OPOST;
import static org.westmalle.wayland.nativ.linux.TermBits.TCIFLUSH;
import static org.westmalle.wayland.nativ.linux.TermBits.TCSANOW;
import static org.westmalle.wayland.nativ.linux.Vt.VT_GETSTATE;
import static org.westmalle.wayland.nativ.linux.Vt.VT_OPENQRY;
import static org.westmalle.wayland.nativ.linux.Vt.VT_PROCESS;
import static org.westmalle.wayland.nativ.linux.Vt.VT_SETMODE;

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

    private Optional<EventSource> setInputMode(final int ttyFd,
                                               final int oldkbMode,
                                               final termios oldTerminalAttributes) {
        final Optional<EventSource> inputSource;
        if (this.libc.ioctl(ttyFd,
                            KDSKBMODE,
                            K_OFF) != 0) {
            LOGGER.warning("Failed to set K_OFF keyboard mode. Fallback to K_RAW.");

            if (this.libc.ioctl(ttyFd,
                                KDSKBMODE,
                                K_RAW) != 0) {
                this.libc.tcsetattr(ttyFd,
                                    TCSANOW,
                                    Pointer.ref(oldTerminalAttributes).address);
                throw new RuntimeException("Failed to set K_RAW keyboard mode.");
            }

            //FIXME add a way in wayland java bindings to check if adding fd failed.
            inputSource = Optional.of(this.display.getEventLoop()
                                                  .addFileDescriptor(ttyFd,
                                                                     WL_EVENT_READABLE,
                                                                     (fd, mask) -> {
                                                                         //Ignore input to tty.  We get keyboard events from libinput
                                                                         this.libc.tcflush(ttyFd,
                                                                                           TCIFLUSH);
                                                                         return 1;
                                                                     }));
        }
        else {
            inputSource = Optional.empty();
        }

        if (this.libc.ioctl(ttyFd,
                            KDSETMODE,
                            KD_GRAPHICS) != 0) {
            inputSource.ifPresent(EventSource::remove);
            this.libc.ioctl(ttyFd,
                            KDSKBMODE,
                            oldkbMode);
            throw new RuntimeException("Failed to set KD_GRAPHICS mode.");
        }

        return inputSource;
    }

    private int getStartupVt(final int ttyFd) {
        final vt_stat vts = new vt_stat();
        if (this.libc.ioctl(ttyFd,
                            VT_GETSTATE,
                            Pointer.ref(vts).address) != 0) {
            throw new RuntimeException("Failed to get VT_GETSTATE.");
        }
        return vts.v_active();
    }

    private int getKbMode(final int ttyFd) {
        final Pointer<Integer> kbModeP = Pointer.nref(0);
        if (this.libc.ioctl(ttyFd,
                            KDGKBMODE,
                            kbModeP.address) != 0) {
            throw new RuntimeException("Failed to get current keyboard mode.");
        }

        return kbModeP.dref();
    }

    private termios getAttributes(final int ttyFd) {
        final termios oldTerminalAttributes = new termios();

        if (this.libc.tcgetattr(ttyFd,
                                Pointer.ref(oldTerminalAttributes).address) < 0) {
            this.libc.close(ttyFd);
            throw new RuntimeException("Could not get terminal attributes.");
        }
        return oldTerminalAttributes;
    }

    private void setAttributes(final int ttyFd,
                               final termios oldTerminalAttributes) {
        //Ignore control characters and disable echo
        final termios newRawAttributes = new termios();
        Pointer.ref(newRawAttributes)
               .write(oldTerminalAttributes);
        this.libc.cfmakeraw(Pointer.ref(newRawAttributes).address);

        //Fix up line endings to be normal (cfmakeraw hoses them)
        newRawAttributes.c_oflag(newRawAttributes.c_oflag() | OPOST | OCRNL);

        if (this.libc.tcsetattr(ttyFd,
                                TCSANOW,
                                Pointer.ref(newRawAttributes).address) < 0) {
            LOGGER.warning("Could not put terminal into raw mode.");
        }
    }

    public Tty create() {
        //TODO tty from config

        final Pointer<Integer> vtNbr = Pointer.nref(0);

        final int ttyFd = getTtyFd(vtNbr);
        final int vt    = vtNbr.dref();

        final termios oldTerminalAttributes = getAttributes(ttyFd);
        setAttributes(ttyFd,
                      oldTerminalAttributes);

        final int oldKbMode = getKbMode(ttyFd);
        final Optional<EventSource> inputSource = setInputMode(ttyFd,
                                                               oldKbMode,
                                                               oldTerminalAttributes);

        final int startupVt = getStartupVt(ttyFd);
        final Tty tty = this.privateTtyFactory.create(ttyFd,
                                                      vt,
                                                      oldKbMode,
                                                      oldTerminalAttributes,
                                                      startupVt);
        if (startupVt != vt) {
            tty.activate();
        }

        inputSource.ifPresent(tty::setInputSource);

        final vt_mode mode = new vt_mode();
        mode.mode(VT_PROCESS);
        mode.relsig(SIGUSR1);
        mode.acqsig(SIGUSR2);
        if (this.libc.ioctl(ttyFd,
                            VT_SETMODE,
                            Pointer.ref(mode).address) < 0) {
            this.libc.ioctl(ttyFd,
                            KDSETMODE,
                            KD_TEXT);
            throw new RuntimeException("failed to take control of vt handling");
        }

        //FIXME add a way in wayland java bindings to check if adding fd failed.
        final EventSource vtSource = this.display.getEventLoop()
                                                 .addSignal(SIGUSR1,
                                                            tty::vtHandler);
        tty.setVtSource(vtSource);

        return tty;
    }
}
