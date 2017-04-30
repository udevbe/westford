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
import org.westford.compositor.core.EglPlatform
import org.westford.compositor.core.events.RenderOutputDestroyed
import org.westford.compositor.core.events.RenderOutputNew
import org.westford.compositor.protocol.WlOutput

@AutoFactory(className = "PrivateX11EglPlatformFactory",
             allowSubclasses = true) class X11EglPlatform(override val wlOutputs: List<WlOutput>,
                                                          override val eglDisplay: Long,
                                                          override val eglContext: Long,
                                                          override val eglExtensions: String) : EglPlatform {
    override val renderOutputNewSignal = Signal<RenderOutputNew>()
    override val renderOutputDestroyedSignal = Signal<RenderOutputDestroyed>()
}
