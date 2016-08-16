package org.westmalle.wayland.drm;

import org.westmalle.wayland.core.events.Signal;
import org.westmalle.wayland.core.events.Slot;

public interface DrmLauncher {

    void switchTty(int vt);

    int openPrivileged(String path, int flags);

    Signal<Void,Slot<Void>> getActivateSignal();

    Signal<Void,Slot<Void>> getDeactivateSignal();
}
