package org.westford.launch


import org.westford.Signal
import org.westford.Slot
import org.westford.compositor.core.events.Activate
import org.westford.compositor.core.events.Deactivate
import org.westford.compositor.core.events.Start
import org.westford.compositor.core.events.Stop

interface LifeCycleSignals {
    val activateSignal: Signal<Activate, Slot<Activate>>

    val deactivateSignal: Signal<Deactivate, Slot<Deactivate>>

    val startSignal: Signal<Start, Slot<Start>>

    val stopSignal: Signal<Stop, Slot<Stop>>
}
