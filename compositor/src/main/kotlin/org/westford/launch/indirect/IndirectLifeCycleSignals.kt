package org.westford.launch.indirect

import com.google.auto.factory.AutoFactory
import com.google.auto.factory.Provided
import org.freedesktop.jaccall.Pointer
import org.freedesktop.jaccall.Size
import org.freedesktop.jaccall.Unsigned
import org.freedesktop.wayland.server.jaccall.WaylandServerCore.WL_EVENT_ERROR
import org.freedesktop.wayland.server.jaccall.WaylandServerCore.WL_EVENT_HANGUP
import org.westford.Signal
import org.westford.compositor.core.events.Activate
import org.westford.compositor.core.events.Deactivate
import org.westford.compositor.core.events.Start
import org.westford.compositor.core.events.Stop
import org.westford.launch.LifeCycleSignals
import org.westford.nativ.glibc.Libc

@AutoFactory(allowSubclasses = true,
             className = "PrivateIndirectLifeCycleSignalsFactory") class IndirectLifeCycleSignals(@param:Provided private val libc: Libc,
                                                                                                  private val launcherFd: Int) : LifeCycleSignals {

    override val activateSignal = Signal<Activate>()
    override val deactivateSignal = Signal<Deactivate>()
    override val startSignal = Signal<Start>()
    override val stopSignal = Signal<Stop>()

    fun handleLauncherEvent(fd: Int,
                            @Unsigned mask: Int): Int {

        if (mask and (WL_EVENT_HANGUP or WL_EVENT_ERROR) != 0) {
            //TODO log
            /* Normally the launcher will reset the tty, but
         * in this case it died or something, so do it here so
		 * we don't end up with a stuck vt. */
            //TODO restore tty & exit
        }

        val ret = Pointer.nref(0)
        var len: Long
        do {
            len = this.libc.recv(this.launcherFd,
                                 ret.address,
                                 Size.sizeof(null as Int?).toLong(),
                                 0)
        }
        while (len < 0 && this.libc.errno == Libc.EINTR)

        when (ret.get()) {
            NativeConstants.EVENT_WESTMALLE_LAUNCHER_ACTIVATE   -> activateSignal.emit(Activate.create())
            NativeConstants.EVENT_WESTMALLE_LAUNCHER_DEACTIVATE -> deactivateSignal.emit(Deactivate.create())
            else                                                -> {/*unsupported event*/
            }
        }
        //TODO log

        return 1
    }
}
