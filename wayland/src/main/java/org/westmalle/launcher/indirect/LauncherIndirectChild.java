package org.westmalle.launcher.indirect;

import org.freedesktop.jaccall.Ptr;
import org.westmalle.Signal;
import org.westmalle.Slot;
import org.westmalle.launcher.Launcher;


public class LauncherIndirectChild implements Launcher {

    //TODO use a unix socket to send privileged calls to the parent and back

    @Override
    public void switchTty(final int vt) {

    }

    @Override
    public int open(@Ptr(String.class) final long path,
                    final int flags) {
        return 0;
    }

    @Override
    public void setDrmMaster(final int fd) {

    }

    @Override
    public void dropDrmMaster(final int fd) {

    }

    @Override
    public Signal<Object, Slot<Object>> getActivateSignal() {
        return null;
    }

    @Override
    public Signal<Object, Slot<Object>> getDeactivateSignal() {
        return null;
    }
}
