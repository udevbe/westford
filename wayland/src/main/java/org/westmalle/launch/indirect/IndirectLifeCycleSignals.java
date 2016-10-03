package org.westmalle.launch.indirect;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import org.freedesktop.jaccall.Pointer;
import org.freedesktop.jaccall.Size;
import org.freedesktop.jaccall.Unsigned;
import org.westmalle.Signal;
import org.westmalle.Slot;
import org.westmalle.launch.LifeCycleSignals;
import org.westmalle.nativ.glibc.Libc;
import org.westmalle.wayland.core.events.Activate;
import org.westmalle.wayland.core.events.Deactivate;

import javax.annotation.Nonnull;

import static org.freedesktop.wayland.server.jaccall.WaylandServerCore.WL_EVENT_ERROR;
import static org.freedesktop.wayland.server.jaccall.WaylandServerCore.WL_EVENT_HANGUP;
import static org.westmalle.nativ.glibc.Libc.EINTR;

@AutoFactory(allowSubclasses = true,
             className = "PrivateIndirectLifeCycleSignalsFactory")
public class IndirectLifeCycleSignals implements LifeCycleSignals {

    private final Signal<Object, Slot<Object>> activateSignal   = new Signal<>();
    private final Signal<Object, Slot<Object>> deactivateSignal = new Signal<>();
    private final Signal<Object, Slot<Object>> startSignal      = new Signal<>();
    private final Signal<Object, Slot<Object>> stopSignal       = new Signal<>();

    @Nonnull
    private final Libc libc;
    private final int  launcherFd;

    public IndirectLifeCycleSignals(@Provided @Nonnull final Libc libc,
                                    final int launcherFd) {
        this.libc = libc;
        this.launcherFd = launcherFd;
    }

    @Override
    public Signal<Object, Slot<Object>> getStartSignal() {
        return this.startSignal;
    }

    @Override
    public Signal<Object, Slot<Object>> getStopSignal() {
        return this.stopSignal;
    }

    public int handleLauncherEvent(final int fd,
                                   @Unsigned final int mask) {


        if ((mask & (WL_EVENT_HANGUP | WL_EVENT_ERROR)) != 0) {
            //TODO log
        /* Normally the launcher will reset the tty, but
         * in this case it died or something, so do it here so
		 * we don't end up with a stuck vt. */
            //TODO restore tty & exit
        }

        final Pointer<Integer> ret = Pointer.nref(0);
        long                   len;
        do {
            len = this.libc.recv(this.launcherFd,
                                 ret.address,
                                 Size.sizeof((Integer) null),
                                 0);
        } while (len < 0 && this.libc.getErrno() == EINTR);

        switch (ret.dref()) {
            case IndirectLauncher.ACTIVATE:
                getActivateSignal().emit(Activate.create());
                break;
            case IndirectLauncher.DEACTIVATE:
                getDeactivateSignal().emit(Deactivate.create());
                break;
            default:
                //unsupported event
                //TODO log
                break;
        }

        return 1;
    }

    @Override
    public Signal<Object, Slot<Object>> getActivateSignal() {
        return this.activateSignal;
    }

    @Override
    public Signal<Object, Slot<Object>> getDeactivateSignal() {
        return this.deactivateSignal;
    }
}
