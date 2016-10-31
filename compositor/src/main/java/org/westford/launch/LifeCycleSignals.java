package org.westford.launch;


import org.westford.Signal;
import org.westford.Slot;
import org.westford.compositor.core.events.Activate;
import org.westford.compositor.core.events.Deactivate;
import org.westford.compositor.core.events.Start;
import org.westford.compositor.core.events.Stop;

public interface LifeCycleSignals {
    Signal<Activate, Slot<Activate>> getActivateSignal();

    Signal<Deactivate, Slot<Deactivate>> getDeactivateSignal();

    Signal<Start, Slot<Start>> getStartSignal();

    Signal<Stop, Slot<Stop>> getStopSignal();
}
