package org.westmalle.launcher;

import org.westmalle.Signal;
import org.westmalle.Slot;

public interface DrmLauncher {

    void switchTty(int vt);

    int openPrivileged(String path,
                       int flags);

    Signal<Object, Slot<Object>> getActivateSignal();

    Signal<Object, Slot<Object>> getDeactivateSignal();
}
