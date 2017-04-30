package org.westford.compositor.core

import org.freedesktop.wayland.server.Display
import org.westford.compositor.core.events.Activate
import org.westford.compositor.core.events.Deactivate
import org.westford.compositor.core.events.Start
import org.westford.compositor.core.events.Stop
import org.westford.compositor.protocol.WlCompositor
import org.westford.compositor.protocol.WlDataDeviceManager
import org.westford.compositor.protocol.WlShell
import org.westford.compositor.protocol.WlSubcompositor
import org.westford.launch.LifeCycleSignals
import javax.inject.Inject

class LifeCycle @Inject internal constructor(private val lifeCycleSignals: LifeCycleSignals,
                                             private val display: Display,
                                             private val jobExecutor: JobExecutor,
                                             private val wlCompositor: WlCompositor,
                                             private val wlDataDeviceManager: WlDataDeviceManager,
                                             private val wlShell: WlShell,
                                             private val wlSubcompositor: WlSubcompositor) {

    fun start() {
        this.jobExecutor.start()
        this.display.initShm()
        this.display.addSocket("wayland-0")
        this.lifeCycleSignals.startSignal.emit(Start.create())
        this.lifeCycleSignals.activateSignal.emit(Activate.create())
        this.display.run()
    }

    fun stop() {
        //FIXME let globals listen for stop signal and cleanup themself, this way we don't have to split this class
        //into LifeCycle (this class) and LifeCycleSignals (which we do to avoid cyclic dependencies).
        this.wlCompositor.destroy()
        this.wlDataDeviceManager.destroy()
        this.wlShell.destroy()
        this.wlSubcompositor.destroy()

        this.lifeCycleSignals.deactivateSignal.emit(Deactivate.create())
        this.lifeCycleSignals.stopSignal.emit(Stop.create())
        this.jobExecutor.fireFinishedEvent()

        this.display.terminate()
    }
}
