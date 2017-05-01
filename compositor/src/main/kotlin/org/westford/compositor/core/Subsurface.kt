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

import com.google.auto.factory.AutoFactory
import org.freedesktop.wayland.server.WlSurfaceResource
import org.westford.Signal
import org.westford.compositor.protocol.WlSurface

@AutoFactory(allowSubclasses = true,
             className = "PrivateSubsurfaceFactory") class Subsurface internal constructor(val parentWlSurfaceResource: WlSurfaceResource,
                                                                                           val sibling: Sibling,
                                                                                           private var currentSurfaceState: SurfaceState,
                                                                                           private var cachedSurfaceState: SurfaceState) : Role {

    val effectiveSyncSignal = Signal<Boolean>()
    var isEffectiveSync = true
        private set

    var isInert = false
        private set
    private var sync = true

    private var position = Point.ZERO

    override fun beforeCommit(wlSurfaceResource: WlSurfaceResource) {
        if (isInert) {
            return
        }

        if (isEffectiveSync) {
            val wlSurface = wlSurfaceResource.implementation as WlSurface
            val surface = wlSurface.surface

            //set back cached state so surface can do eg. buffer release
            surface.state = this.cachedSurfaceState
        }
    }

    override fun accept(roleVisitor: RoleVisitor) {
        roleVisitor.visit(this)
    }

    fun setInert() {
        this.isInert = true
    }

    fun onParentApply() {
        if (isInert) {
            return
        }

        if (isEffectiveSync && this.currentSurfaceState != this.cachedSurfaceState) {
            //sync mode. update current state with cached state
            this.currentSurfaceState = this.cachedSurfaceState
            apply(this.cachedSurfaceState)
        }

        //only triggered on parent commit, regardless of mode.
        sibling.position = this.position
    }

    fun apply(surfaceState: SurfaceState) {
        if (isInert) {
            return
        }

        val wlSurface = sibling.wlSurfaceResource.implementation as WlSurface
        val surface = wlSurface.surface

        if (isEffectiveSync) {

            if (surface.state != this.currentSurfaceState) {
                //roll back 'to-be' state to current active state in case of non-parent commit.
                //In case of parent commit, currentSurfaceState will be set to the accumulated cachedSurfaceState.
                surface.apply(this.currentSurfaceState)
                //cache to-be state.
                this.cachedSurfaceState = surfaceState
            }
        }
        else {
            //desync mode, our to-be state is always the current state.
            this.cachedSurfaceState = surfaceState
            this.currentSurfaceState = surfaceState
        }
    }

    fun setSync(sync: Boolean) {
        if (isInert) {
            return
        }

        this.sync = sync

        val parentWlSurface = parentWlSurfaceResource.implementation as WlSurface
        parentWlSurface.surface.role.ifPresent { role ->
            role.accept(object : RoleVisitor {
                override fun visit(parentSubsurface: Subsurface) {
                    //TODO unit test this
                    updateEffectiveSync(parentSubsurface.isEffectiveSync)
                }

                override fun defaultAction(role: Role) {
                    updateEffectiveSync(false)
                }
            })
        }
    }

    fun updateEffectiveSync(parentEffectiveSync: Boolean) {
        val oldEffectiveSync = this.isEffectiveSync
        this.isEffectiveSync = this.sync || parentEffectiveSync

        if (oldEffectiveSync != isEffectiveSync) {
            /*
             * If we were in sync mode and now our effective mode is desync, we have to apply our cached state
             * immediately
             */
            //TODO unit test this
            if (!isEffectiveSync) {
                val wlSurface = sibling.wlSurfaceResource.implementation as WlSurface
                wlSurface.surface.apply(this.cachedSurfaceState)
            }

            effectiveSyncSignal.emit(isEffectiveSync)
        }
    }

    fun above(sibling: WlSurfaceResource) {
        if (isInert) {
            return
        }

        placement(false,
                  sibling)
    }

    private fun placement(below: Boolean,
                          siblingWlSurfaceResource: WlSurfaceResource) {

        val parentWlSurfaceResource = parentWlSurfaceResource
        val parentWlSurface = parentWlSurfaceResource.implementation as WlSurface
        val parentSurface = parentWlSurface.surface
        val siblings = parentSurface.siblings

        var siblingIndex = -1
        var thisIndex = -1

        for (i in siblings.indices) {
            val sibling = siblings[i]

            if (sibling == Sibling.create(siblingWlSurfaceResource)) {
                siblingIndex = i
            }
            else if (sibling == sibling) {
                thisIndex = i
            }

            if (siblingIndex != -1 && thisIndex != -1) {
                break
            }
        }

        //FIXME if siblingIndex == -1 then we have a (client) protocol error, else we have a bug.

        siblings.add(if (below) siblingIndex else siblingIndex + 1,
                     siblings.removeAt(thisIndex))

        //Note: committing the subsurface stack happens in the parent surface.
    }

    fun below(sibling: WlSurfaceResource) {
        if (isInert) {
            return
        }

        placement(true,
                  sibling)
    }

    fun setPosition(position: Point) {
        if (isInert) {
            return
        }

        this.position = position
    }
}
