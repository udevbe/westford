package org.westmalle.launcher;


import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import org.freedesktop.jaccall.Pointer;
import org.westmalle.Signal;
import org.westmalle.Slot;
import org.westmalle.nativ.glibc.Libc;
import org.westmalle.tty.Tty;

import javax.annotation.Nonnull;

@AutoFactory(allowSubclasses = true,
             className = "PrivateDrmLauncherDirectFactory")
public class DrmLauncherDirect implements DrmLauncher {

    private final Signal<Object, Slot<Object>> activateSignal   = new Signal<>();
    private final Signal<Object, Slot<Object>> deactivateSignal = new Signal<>();

    @Nonnull
    private final Libc libc;
    @Nonnull
    private final Tty  tty;

    DrmLauncherDirect(@Provided @Nonnull final Libc libc,
                      @Provided @Nonnull final Tty tty) {
        this.libc = libc;
        this.tty = tty;
    }

    @Override
    public void switchTty(final int vt) {
        this.tty.activate(vt);
    }

    @Override
    public int openPrivileged(final String path,
                              final int flags) {
        return this.libc.open(Pointer.nref(path).address,
                              flags);
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
