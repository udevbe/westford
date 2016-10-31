package org.westmalle.launch;


import org.westmalle.Signal;
import org.westmalle.Slot;
import org.westmalle.compositor.core.events.Activate;
import org.westmalle.compositor.core.events.Deactivate;
import org.westmalle.compositor.core.events.Start;
import org.westmalle.compositor.core.events.Stop;

public interface LifeCycleSignals {
    Signal<Activate, Slot<Activate>> getActivateSignal();

    Signal<Deactivate, Slot<Deactivate>> getDeactivateSignal();

    Signal<Start, Slot<Start>> getStartSignal();

    Signal<Stop, Slot<Stop>> getStopSignal();
}
