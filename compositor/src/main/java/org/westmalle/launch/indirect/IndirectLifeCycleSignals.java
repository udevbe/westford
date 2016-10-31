package org.westmalle.launch.indirect;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import org.freedesktop.jaccall.Pointer;
import org.freedesktop.jaccall.Size;
import org.freedesktop.jaccall.Unsigned;
import org.westmalle.Signal;
import org.westmalle.Slot;
import org.westmalle.compositor.core.events.Activate;
import org.westmalle.compositor.core.events.Deactivate;
import org.westmalle.compositor.core.events.Start;
import org.westmalle.compositor.core.events.Stop;
import org.westmalle.launch.LifeCycleSignals;
import org.westmalle.nativ.glibc.Libc;

import javax.annotation.Nonnull;

import static org.freedesktop.wayland.server.jaccall.WaylandServerCore.WL_EVENT_ERROR;
import static org.freedesktop.wayland.server.jaccall.WaylandServerCore.WL_EVENT_HANGUP;
import static org.westmalle.launch.indirect.NativeConstants.EVENT_WESTMALLE_LAUNCHER_ACTIVATE;
import static org.westmalle.launch.indirect.NativeConstants.EVENT_WESTMALLE_LAUNCHER_DEACTIVATE;
import static org.westmalle.nativ.glibc.Libc.EINTR;

@AutoFactory(allowSubclasses = true,
             className = "PrivateIndirectLifeCycleSignalsFactory")
public class IndirectLifeCycleSignals implements LifeCycleSignals {

    private final Signal<Activate, Slot<Activate>>     activateSignal   = new Signal<>();
    private final Signal<Deactivate, Slot<Deactivate>> deactivateSignal = new Signal<>();
    private final Signal<Start, Slot<Start>>           startSignal      = new Signal<>();
    private final Signal<Stop, Slot<Stop>>             stopSignal       = new Signal<>();

    @Nonnull
    private final Libc libc;
    private final int  launcherFd;

    public IndirectLifeCycleSignals(@Provided @Nonnull final Libc libc,
                                    final int launcherFd) {
        this.libc = libc;
        this.launcherFd = launcherFd;
    }

    @Override
    public Signal<Start, Slot<Start>> getStartSignal() {
        return this.startSignal;
    }

    @Override
    public Signal<Stop, Slot<Stop>> getStopSignal() {
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
            case EVENT_WESTMALLE_LAUNCHER_ACTIVATE:
                getActivateSignal().emit(Activate.create());
                break;
            case EVENT_WESTMALLE_LAUNCHER_DEACTIVATE:
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
    public Signal<Activate, Slot<Activate>> getActivateSignal() {
        return this.activateSignal;
    }

    @Override
    public Signal<Deactivate, Slot<Deactivate>> getDeactivateSignal() {
        return this.deactivateSignal;
    }
}
