package org.westmalle.launch;


import org.westmalle.Signal;
import org.westmalle.Slot;

public interface LifeCycleSignals {
    Signal<Object, Slot<Object>> getActivateSignal();

    Signal<Object, Slot<Object>> getDeactivateSignal();

    Signal<Object, Slot<Object>> getStartSignal();

    Signal<Object, Slot<Object>> getStopSignal();
}
