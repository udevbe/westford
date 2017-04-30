package org.westford.launch

import org.westford.Signal
import org.westford.compositor.core.events.Activate
import org.westford.compositor.core.events.Deactivate
import org.westford.compositor.core.events.Start
import org.westford.compositor.core.events.Stop

interface LifeCycleSignals {
    val activateSignal: Signal<Activate>

    val deactivateSignal: Signal<Deactivate>

    val startSignal: Signal<Start>

    val stopSignal: Signal<Stop>
}
