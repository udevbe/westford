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
package org.westford.compositor.drm

import com.google.auto.factory.AutoFactory
import org.westford.Signal
import org.westford.Slot
import org.westford.compositor.core.events.RenderOutputDestroyed
import org.westford.compositor.core.events.RenderOutputNew

//TODO drm platform, remove all gbm dependencies
@AutoFactory(allowSubclasses = true, className = "PrivateDrmPlatformFactory")
class DrmPlatform internal constructor(val drmDevice: Long,
                                       val drmFd: Int,
                                       val drmEventBus: DrmEventBus,
                                       val renderOutputs: List<DrmOutput>) {
    val renderOutputNewSignal = Signal<RenderOutputNew, Slot<RenderOutputNew>>()
    val renderOutputDestroyedSignal = Signal<RenderOutputDestroyed, Slot<RenderOutputDestroyed>>()
}
