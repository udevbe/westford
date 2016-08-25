package org.westmalle.launcher.direct;


import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import org.freedesktop.jaccall.Pointer;
import org.freedesktop.jaccall.Ptr;
import org.westmalle.Signal;
import org.westmalle.Slot;
import org.westmalle.launcher.Launcher;
import org.westmalle.nativ.glibc.Libc;
import org.westmalle.nativ.libdrm.Libdrm;
import org.westmalle.tty.Tty;

import javax.annotation.Nonnull;

@AutoFactory(allowSubclasses = true,
             className = "PrivateDrmLauncherDirectFactory")
public class LauncherDirect implements Launcher {

    private final Signal<Object, Slot<Object>> activateSignal   = new Signal<>();
    private final Signal<Object, Slot<Object>> deactivateSignal = new Signal<>();

    @Nonnull
    private final Libc   libc;
    @Nonnull
    private final Libdrm libdrm;
    @Nonnull
    private final Tty    tty;

    LauncherDirect(@Provided @Nonnull final Libc libc,
                   @Provided @Nonnull final Libdrm libdrm,
                   @Provided @Nonnull final Tty tty) {
        this.libc = libc;
        this.libdrm = libdrm;
        this.tty = tty;
    }

    @Override
    public void switchTty(final int vt) {
        this.tty.activate(vt);
    }

    @Override
    public int open(@Ptr(String.class) final long path,
                    final int flags) {
        return this.libc.open(Pointer.nref(path).address,
                              flags);
    }

    @Override
    public void setDrmMaster(final int fd) {
        if (this.libdrm.drmSetMaster(fd) != 0) {
            throw new RuntimeException("failed to set drm master.");
        }
    }

    @Override
    public void dropDrmMaster(final int fd) {
        if (this.libdrm.drmDropMaster(fd) != 0) {
            throw new RuntimeException("failed to drop drm master.");
        }
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
