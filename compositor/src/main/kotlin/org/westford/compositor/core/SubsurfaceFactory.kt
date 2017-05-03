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

import org.freedesktop.wayland.server.WlSurfaceResource
import org.westford.compositor.protocol.WlSurface
import javax.inject.Inject

class SubsurfaceFactory @Inject internal constructor(private val privateSubsurfaceFactory: PrivateSubsurfaceFactory) {

    fun create(parentWlSurfaceResource: WlSurfaceResource,
               wlSurfaceResource: WlSurfaceResource): Subsurface {

        val wlSurface = wlSurfaceResource.implementation as WlSurface
        val surface = wlSurface.surface

        val surfaceState = surface.state
        val subsurface = this.privateSubsurfaceFactory.create(parentWlSurfaceResource,
                                                              Sibling.create(wlSurfaceResource),
                                                              surfaceState,
                                                              surfaceState)
        surface.applySurfaceStateSignal.connect {
            subsurface.apply(it)
        }

        val parentWlSurface = parentWlSurfaceResource.implementation as WlSurface
        val parentSurface = parentWlSurface.surface

        parentSurface.applySurfaceStateSignal.connect {
            subsurface.onParentApply()
        }

        parentSurface.role?.accept(object : RoleVisitor {
            override fun visit(subsurface: Subsurface) {
                subsurface.effectiveSyncSignal.connect {
                    subsurface.updateEffectiveSync(it)
                }
            }
        })

        parentSurface.addSubsurface(subsurface)

        /*
         * Docs says a subsurface with a destroyed parent must become inert.
         */
        parentWlSurfaceResource.register { subsurface.setInert() }

        parentSurface.pendingSubsurfaces.add(subsurface)

        return subsurface
    }
}
