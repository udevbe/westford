package org.westmalle.wayland.tty;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import org.freedesktop.jaccall.Pointer;
import org.freedesktop.wayland.server.EventSource;
import org.westmalle.wayland.core.events.Signal;
import org.westmalle.wayland.core.events.Slot;
import org.westmalle.wayland.nativ.glibc.Libc;
import org.westmalle.wayland.nativ.glibc.termios;
import org.westmalle.wayland.nativ.linux.vt_mode;

import java.util.Optional;
import java.util.logging.Logger;

import static org.westmalle.wayland.nativ.linux.Kd.KDSETMODE;
import static org.westmalle.wayland.nativ.linux.Kd.KDSKBMODE;
import static org.westmalle.wayland.nativ.linux.Kd.KD_TEXT;
import static org.westmalle.wayland.nativ.linux.TermBits.TCSANOW;
import static org.westmalle.wayland.nativ.linux.Vt.VT_ACKACQ;
import static org.westmalle.wayland.nativ.linux.Vt.VT_ACTIVATE;
import static org.westmalle.wayland.nativ.linux.Vt.VT_AUTO;
import static org.westmalle.wayland.nativ.linux.Vt.VT_RELDISP;
import static org.westmalle.wayland.nativ.linux.Vt.VT_SETMODE;
import static org.westmalle.wayland.nativ.linux.Vt.VT_WAITACTIVE;

@AutoFactory(className = "PrivateTtyFactory",
             allowSubclasses = true)
public class Tty implements AutoCloseable {

    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    private final Libc libc;
    private final int  ttyFd;
    private final int  vt;

    private final int     oldKbMode;
    private final termios oldTerminalAttributes;
    private final int     startupVt;

    private final Signal<VtEnter, Slot<VtEnter>> vtEnterSignal = new Signal<>();
    private final Signal<VtLeave, Slot<VtLeave>> vtLeaveSignal = new Signal<>();

    private boolean vtActive;
    private Optional<EventSource> inputSource = Optional.empty();
    private Optional<EventSource> vtSource    = Optional.empty();

    Tty(@Provided final Libc libc,
        final int ttyFd,
        final int vt,
        final int oldKbMode,
        final termios oldTerminalAttributes,
        final int startupVt) {

        this.libc = libc;
        this.ttyFd = ttyFd;
        this.vt = vt;
        this.oldKbMode = oldKbMode;
        this.oldTerminalAttributes = oldTerminalAttributes;
        this.startupVt = startupVt;
    }

    public void activate() {
        activate(this.vt);
    }

    public void activate(final int vt) {
        LOGGER.info("Switching to vt:" + vt);
        if (this.libc.ioctl(this.ttyFd,
                            VT_ACTIVATE,
                            vt) < 0 ||
            this.libc.ioctl(this.ttyFd,
                            VT_WAITACTIVE,
                            vt) < 0) {
            throw new RuntimeException("failed to switch to new vt.");
        }
    }

    public int vtHandler(final int signalNumber) {
        if (this.vtActive) {
            LOGGER.info("Leaving our vt:" + this.vt);

            this.vtActive = false;
            this.vtLeaveSignal.emit(VtLeave.create());

            this.libc.ioctl(this.ttyFd,
                            VT_RELDISP,
                            1);
        }
        else {
            LOGGER.info("Entering our vt:" + this.vt);

            this.libc.ioctl(this.ttyFd,
                            VT_RELDISP,
                            VT_ACKACQ);

            this.vtActive = true;
            this.vtEnterSignal.emit(VtEnter.create());
        }

        return 1;
    }

    public boolean isVtActive() {
        return this.vtActive;
    }

    public Signal<VtEnter, Slot<VtEnter>> getVtEnterSignal() {
        return this.vtEnterSignal;
    }

    public Signal<VtLeave, Slot<VtLeave>> getVtLeaveSignal() {
        return this.vtLeaveSignal;
    }

    @Override
    public void close() {
        //tear down tty

        this.inputSource.ifPresent(eventSource -> {
            eventSource.remove();
            this.inputSource = Optional.empty();
        });

        if (this.libc.ioctl(this.ttyFd,
                            KDSKBMODE,
                            this.oldKbMode) != 0) {
            LOGGER.severe("failed to restore keyboard mode");
        }

        if (this.libc.ioctl(this.ttyFd,
                            KDSETMODE,
                            KD_TEXT) != 0) {
            LOGGER.severe("failed to set KD_TEXT mode on tty");
        }

        if (this.libc.tcsetattr(this.ttyFd,
                                TCSANOW,
                                Pointer.ref(this.oldTerminalAttributes).address) < 0) {
            LOGGER.severe("could not restore terminal to canonical mode");
        }

        final vt_mode mode = new vt_mode();
        mode.mode(VT_AUTO);
        mode.waitv((byte) 0);
        mode.relsig((byte) 0);
        mode.acqsig((byte) 0);
        mode.frsig((byte) 0);

        if (this.libc.ioctl(this.ttyFd,
                            VT_SETMODE,
                            Pointer.ref(mode).address) < 0) {
            LOGGER.severe("could not reset vt handling");
        }

        if (this.vtActive && this.vt != this.startupVt) {
            this.libc.ioctl(this.ttyFd,
                            VT_ACTIVATE,
                            this.startupVt);
            this.libc.ioctl(this.ttyFd,
                            VT_WAITACTIVE,
                            this.startupVt);
        }

        this.vtSource.ifPresent(eventSource -> {
            eventSource.remove();
            this.vtSource = Optional.empty();
        });

        //FIXME check close state?
        this.libc.close(this.ttyFd);
    }

    void setInputSource(final EventSource inputSource) {
        this.inputSource = Optional.of(inputSource);
    }

    void setVtSource(final EventSource vtSource) {
        this.vtSource = Optional.of(vtSource);
    }
}
