package org.westmalle.wayland.tty;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import org.westmalle.wayland.core.events.Signal;
import org.westmalle.wayland.core.events.Slot;
import org.westmalle.wayland.nativ.libc.Libc;

import static org.westmalle.wayland.nativ.linux.TermBits.TCIFLUSH;
import static org.westmalle.wayland.nativ.linux.Vt.VT_ACKACQ;
import static org.westmalle.wayland.nativ.linux.Vt.VT_RELDISP;

@AutoFactory(className = "PrivateTtyFactory",
             allowSubclasses = true)
public class Tty {

    private final Libc libc;
    private final int  fd;

    private final Signal<VtEnter, Slot<VtEnter>> vtEnterSignal = new Signal<>();
    private final Signal<VtLeave, Slot<VtLeave>> vtLeaveSignal = new Signal<>();

    private boolean vtActive;

    Tty(@Provided final Libc libc,
        final int fd) {
        this.libc = libc;
        this.fd = fd;
    }

    public int onTtyInput(final int fd,
                          final int mask) {
        /* Ignore input to tty.  We get keyboard events from evdev
         */
        this.libc.tcflush(this.fd,
                          TCIFLUSH);

        return 1;
    }

    public int vtHandler(final int signalNumber) {
        if (this.vtActive) {
            this.vtActive = false;
            this.vtLeaveSignal.emit(VtLeave.create());

            this.libc.ioctl(this.fd,
                            VT_RELDISP,
                            1);
        }
        else {
            this.libc.ioctl(this.fd,
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
}
