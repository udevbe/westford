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
package org.westford.compositor.core

import org.freedesktop.wayland.server.WlSeatResource
import org.freedesktop.wayland.shared.WlSeatCapability
import javax.inject.Inject
import java.util.EnumSet

class Seat @Inject
internal constructor() {

    private var capabilities = EnumSet.noneOf<WlSeatCapability>(WlSeatCapability::class.java)

    fun emitCapabilities(wlSeatResources: Set<WlSeatResource>) {
        val capabilitiesFlag = capabilitiesFlag()
        wlSeatResources.forEach { wlSeatResource -> wlSeatResource.capabilities(capabilitiesFlag) }
    }

    private fun capabilitiesFlag(): Int {
        var flag = 0
        for (capability in this.capabilities) {
            flag = flag or capability.value
        }
        return flag
    }

    fun setCapabilities(capability: EnumSet<WlSeatCapability>) {
        this.capabilities = capability
    }
}
