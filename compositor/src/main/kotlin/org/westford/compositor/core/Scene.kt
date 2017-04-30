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
import javax.inject.Singleton

@Singleton class Scene @Inject internal constructor(val backgroundLayer: SceneLayer,
                                                    val underLayer: SceneLayer,
                                                    val applicationLayer: SceneLayer,
                                                    val overLayer: SceneLayer,
                                                    val fullscreenLayer: SceneLayer,
                                                    val lockLayer: SceneLayer,
                                                    val cursorLayer: SceneLayer,
                                                    private val infiniteRegion: InfiniteRegion) {

    fun pickSurfaceView(global: Point): SurfaceView? {

        var pointerOver: SurfaceView? = null

        pickableSurfaces().asReversed().forEach lit@ {
            if (it.isDrawable && it.isEnabled) {
                val implementation = it.wlSurfaceResource.implementation as WlSurface
                val surface = implementation.surface

                val inputRegion = surface.state.inputRegion
                val region = inputRegion ?: this.infiniteRegion

                val size = surface.size

                val local = it.local(global)
                if (region.contains(size,
                                    local)) {
                    pointerOver = it
                    return@lit
                }
            }
        }

        return pointerOver
    }

    fun pickableSurfaces(): MutableList<SurfaceView> {

        val views = mutableListOf<SurfaceView>()

        if (!this.lockLayer.surfaceViews.isEmpty()) {
            //lockLayer screen
            views.addAll(this.lockLayer.surfaceViews)
        }
        else {
            views.addAll(this.backgroundLayer.surfaceViews)
            views.addAll(this.underLayer.surfaceViews)
            views.addAll(this.applicationLayer.surfaceViews)
            views.addAll(this.overLayer.surfaceViews)
            views.addAll(this.fullscreenLayer.surfaceViews)
        }

        //make sure we include any sub-views
        val pickableViews = mutableListOf<SurfaceView>()
        views.forEach {
            pickableViews.addAll(withSiblingViews(it))
        }

        return pickableViews
    }

    /**
     * Return all views who at least have a partial intersection with the given region.
     *
     *
     * Sibling views are not iterated explicitly.

     * @param views
     * *
     * @param region
     * *
     * *
     * @return
     */
    fun subsection(views: List<SurfaceView>,
                   region: Region): List<SurfaceView> {

        val intersectingViews = mutableListOf<SurfaceView>()

        views.forEach {
            val wlSurface = it.wlSurfaceResource.implementation as WlSurface
            val surface = wlSurface.surface
            val size = surface.size

            val viewBox = Rectangle.create(it.global(Point.ZERO),
                                           size.width,
                                           size.height)

            if (region.contains(viewBox)) {
                intersectingViews.add(it)
            }
        }

        return intersectingViews
    }

    /**
     * Return all views from the layer who at least have a partial intersection with the given region.
     *
     *
     * Sibling views are iterated explicitly.

     * @param sceneLayer
     * *
     * @param region
     * *
     * *
     * @return
     */
    fun subsection(sceneLayer: SceneLayer,
                   region: Region): List<SurfaceView> {
        val views = mutableListOf<SurfaceView>()
        sceneLayer.surfaceViews.forEach {
            views.addAll(withSiblingViews(it))
        }
        return subsection(views,
                          region)
    }

    /**
     * Create a subscene where all returned views are at least partially visible on the given output.

     * @param region
     * *
     * *
     * @return
     */
    fun subsection(region: Region): Subscene {

        val outputScene: Subscene

        if (!this.lockLayer.surfaceViews.isEmpty()) {
            val outputLockViews = subsection(this.lockLayer,
                                             region)
            val cursorViews = subsection(this.cursorLayer,
                                         region)
            outputScene = Subscene.create(null,
                                          emptyList<SurfaceView>(),
                                          emptyList<SurfaceView>(),
                                          emptyList<SurfaceView>(),
                                          null,
                                          outputLockViews,
                                          cursorViews)
        }
        else {

            val backgroundView: SurfaceView?
            val underViews: List<SurfaceView>
            val applicationViews: List<SurfaceView>
            val overViews: List<SurfaceView>
            val fullscreenView: SurfaceView?

            val outputFullscreenViews = subsection(this.fullscreenLayer,
                                                   region)
            val cursorViews = subsection(this.cursorLayer,
                                         region)
            if (outputFullscreenViews.isEmpty()) {
                val outputBackgroundViews = subsection(this.backgroundLayer,
                                                       region)
                val outputUnderViews = subsection(this.underLayer,
                                                  region)
                val outputApplicationViews = subsection(this.applicationLayer,
                                                        region)
                val outputOverViews = subsection(this.overLayer,
                                                 region)

                backgroundView = outputBackgroundViews.firstOrNull()
                underViews = outputUnderViews
                applicationViews = outputApplicationViews
                overViews = outputOverViews
                fullscreenView = null
            }
            else {
                //there is a fullscreen view, don't bother return the underlying views
                backgroundView = null
                underViews = emptyList<SurfaceView>()
                applicationViews = emptyList<SurfaceView>()
                overViews = emptyList<SurfaceView>()
                fullscreenView = outputFullscreenViews.first()
            }

            outputScene = Subscene.create(backgroundView,
                                          underViews,
                                          applicationViews,
                                          overViews,
                                          fullscreenView,
                                          emptyList<SurfaceView>(),
                                          cursorViews)
        }

        return outputScene
    }

    /**
     * All surfaces, including siblings.

     * @return
     */
    fun allSurfaces(): List<SurfaceView> {

        val drawableSurfaceViewStack = pickableSurfaces()
        //add cursor surfaces
        this.cursorLayer.surfaceViews.forEach {
            drawableSurfaceViewStack += withSiblingViews(it)
        }

        return drawableSurfaceViewStack
    }

    /**
     * Expand a view so the returned list also includes its siblings.

     * @param surfaceView
     * *
     * *
     * @return
     */
    fun withSiblingViews(surfaceView: SurfaceView): List<SurfaceView> {
        val surfaceViews = mutableListOf<SurfaceView>()
        addSiblingViews(surfaceView,
                        surfaceViews)
        return surfaceViews
    }

    /**
     * Gather all parent surface views, including the parent surface view and insert it with a correct order into the provided list.

     * @param parentSurfaceView
     * *
     * @param surfaceViews
     */
    private fun addSiblingViews(parentSurfaceView: SurfaceView,
                                surfaceViews: MutableList<SurfaceView>) {

        val parentWlSurfaceResource = parentSurfaceView.wlSurfaceResource
        val parentWlSurface = parentWlSurfaceResource.implementation as WlSurface
        val parentSurface = parentWlSurface.surface

        parentSurface.siblings.forEach {

            val siblingWlSurface = it.wlSurfaceResource.implementation as WlSurface
            val siblingSurface = siblingWlSurface.surface

            //only consider surface if it has a role.
            //TODO we could move the views to the generic role itf.
            if (siblingSurface.role != null) {

                siblingSurface.views.forEach {
                    if (it.parent?.takeIf { it == parentSurfaceView } != null) {
                        addSiblingViews(it,
                                        surfaceViews)
                    }
                    else if (it == parentSurfaceView) {
                        surfaceViews.add(0,
                                         it)
                    }
                }
            }
        }
    }

    fun removeView(surfaceView: SurfaceView) {
        this.backgroundLayer.surfaceViews.remove(surfaceView)
        this.underLayer.surfaceViews.remove(surfaceView)
        this.applicationLayer.surfaceViews.remove(surfaceView)
        this.overLayer.surfaceViews.remove(surfaceView)
        this.fullscreenLayer.surfaceViews.remove(surfaceView)
        this.lockLayer.surfaceViews.remove(surfaceView)
    }

    fun removeAllViews(wlSurfaceResource: WlSurfaceResource) {
        val wlSurface = wlSurfaceResource.implementation as WlSurface
        val surface = wlSurface.surface

        val views = surface.views
        views.forEach {
            this.removeView(it)
        }
    }
}
