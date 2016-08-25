package org.westmalle.launcher;

import org.freedesktop.jaccall.Ptr;
import org.westmalle.Signal;
import org.westmalle.Slot;

public interface DrmLauncher {

    void switchTty(int vt);

    int open(@Ptr(String.class) long path,
             int flags);

    void setDrmMaster(int fd);

    void dropDrmMaster(int fd);

    Signal<Object, Slot<Object>> getActivateSignal();

    Signal<Object, Slot<Object>> getDeactivateSignal();
}
