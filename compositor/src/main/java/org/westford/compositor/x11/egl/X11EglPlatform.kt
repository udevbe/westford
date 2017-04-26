/*
 * Westford Wayland Compositor.
 * Copyright (C) 2016  Erik De Rijcke
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.westford.compositor.x11.egl

import com.google.auto.factory.AutoFactory
import org.westford.Signal
import org.westford.Slot
import org.westford.compositor.core.EglPlatform
import org.westford.compositor.core.events.RenderOutputDestroyed
import org.westford.compositor.core.events.RenderOutputNew
import org.westford.compositor.protocol.WlOutput

@AutoFactory(className = "PrivateX11EglPlatformFactory", allowSubclasses = true)
class X11EglPlatform internal constructor(private val wlOutputs: List<WlOutput>,
                                          private val eglDisplay: Long,
                                          private val eglContext: Long,
                                          private val eglExtensions: String) : EglPlatform {
    private val renderOutputNewSignal = Signal<RenderOutputNew, Slot<RenderOutputNew>>()
    private val renderOutputDestroyedSignal = Signal<RenderOutputDestroyed, Slot<RenderOutputDestroyed>>()

    override fun getEglDisplay(): Long {
        return this.eglDisplay
    }

    override fun getEglContext(): Long {
        return this.eglContext
    }

    override fun getWlOutputs(): List<WlOutput> {
        return this.wlOutputs
    }

    override fun getRenderOutputNewSignal(): Signal<RenderOutputNew, Slot<RenderOutputNew>> {
        return this.renderOutputNewSignal
    }

    override fun getRenderOutputDestroyedSignal(): Signal<RenderOutputDestroyed, Slot<RenderOutputDestroyed>> {
        return this.renderOutputDestroyedSignal
    }

    override fun getEglExtensions(): String {
        return this.eglExtensions
    }
}
