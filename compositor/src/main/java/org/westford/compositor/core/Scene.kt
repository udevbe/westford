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
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton class Scene @Inject internal constructor(val backgroundLayer: SceneLayer, val underLayer: SceneLayer,
                                                    val applicationLayer: SceneLayer, val overLayer: SceneLayer,
                                                    val fullscreenLayer: SceneLayer, val lockLayer: SceneLayer,
                                                    val cursorLayer: SceneLayer,
                                                    private val infiniteRegion: InfiniteRegion) {

    fun pickSurfaceView(global: Point): SurfaceView? {

        val surfaceViewIterator = pickableSurfaces().descendingIterator()
        var pointerOver = Optional.empty<SurfaceView>()

        while (surfaceViewIterator.hasNext()) {
            val surfaceView = surfaceViewIterator.next()

            if (!surfaceView.isDrawable || !surfaceView.isEnabled) {
                continue
            }

            val surfaceResource = surfaceView.wlSurfaceResource
            val implementation = surfaceResource.implementation
            val surface = (implementation as WlSurface).surface

            val inputRegion = surface.state.inputRegion
            val region = inputRegion.orElse(this.infiniteRegion)

            val size = surface.size

            val local = surfaceView.local(global)
            if (region.contains(size, local)) {
                pointerOver = Optional.of(surfaceView)
                break
            }
        }

        return pointerOver
    }

    fun pickableSurfaces(): LinkedList<SurfaceView> {

        val views = LinkedList<SurfaceView>()

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
        val pickableViews = LinkedList<SurfaceView>()
        views.forEach { surfaceView -> pickableViews.addAll(withSiblingViews(surfaceView)) }

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
    fun subsection(views: LinkedList<SurfaceView>, region: Region): LinkedList<SurfaceView> {

        val intersectingViews = LinkedList<SurfaceView>()

        views.forEach { surfaceView ->
            val wlSurfaceResource = surfaceView.wlSurfaceResource
            val wlSurface = wlSurfaceResource.implementation as WlSurface
            val surface = wlSurface.surface
            val size = surface.size

            val viewBox = Rectangle.create(surfaceView.global(Point.ZERO), size.width, size.height)

            if (region.contains(viewBox)) {
                intersectingViews.add(surfaceView)
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
    fun subsection(sceneLayer: SceneLayer, region: Region): LinkedList<SurfaceView> {
        val views = LinkedList<SurfaceView>()
        sceneLayer.surfaceViews.forEach { surfaceView -> views.addAll(withSiblingViews(surfaceView)) }
        return subsection(views, region)
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
            val outputLockViews = subsection(this.lockLayer, region)
            val cursorViews = subsection(this.cursorLayer, region)
            outputScene = Subscene.create(Optional.empty<SurfaceView>(), emptyList<SurfaceView>(),
                                          emptyList<SurfaceView>(), emptyList<SurfaceView>(),
                                          Optional.empty<SurfaceView>(), outputLockViews, cursorViews)
        }
        else {

            val backgroundView: Optional<SurfaceView>
            val underViews: List<SurfaceView>
            val applicationViews: List<SurfaceView>
            val overViews: List<SurfaceView>
            val fullscreenView: Optional<SurfaceView>

            val outputFullscreenViews = subsection(this.fullscreenLayer, region)
            val cursorViews = subsection(this.cursorLayer, region)
            if (outputFullscreenViews.isEmpty()) {
                val outputBackgroundViews = subsection(this.backgroundLayer, region)
                val outputUnderViews = subsection(this.underLayer, region)
                val outputApplicationViews = subsection(this.applicationLayer, region)
                val outputOverViews = subsection(this.overLayer, region)

                backgroundView = Optional.ofNullable(outputBackgroundViews.peekFirst())
                underViews = outputUnderViews
                applicationViews = outputApplicationViews
                overViews = outputOverViews
                fullscreenView = Optional.empty<SurfaceView>()
            }
            else {
                //there is a fullscreen view, don't bother return the underlying views
                backgroundView = Optional.empty<SurfaceView>()
                underViews = emptyList<SurfaceView>()
                applicationViews = emptyList<SurfaceView>()
                overViews = emptyList<SurfaceView>()
                fullscreenView = Optional.ofNullable(outputFullscreenViews.first)
            }

            outputScene = Subscene.create(backgroundView, underViews, applicationViews, overViews, fullscreenView,
                                          emptyList<SurfaceView>(), cursorViews)
        }

        return outputScene
    }

    /**
     * All surfaces, including siblings.

     * @return
     */
    fun allSurfaces(): LinkedList<SurfaceView> {

        val drawableSurfaceViewStack = pickableSurfaces()
        //add cursor surfaces
        this.cursorLayer.surfaceViews.forEach { cursorSurfaceView ->
            drawableSurfaceViewStack.addAll(withSiblingViews(cursorSurfaceView))
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
    fun withSiblingViews(surfaceView: SurfaceView): LinkedList<SurfaceView> {
        val surfaceViews = LinkedList<SurfaceView>()
        addSiblingViews(surfaceView, surfaceViews)
        return surfaceViews
    }

    /**
     * Gather all parent surface views, including the parent surface view and insert it with a correct order into the provided list.

     * @param parentSurfaceView
     * *
     * @param surfaceViews
     */
    private fun addSiblingViews(parentSurfaceView: SurfaceView, surfaceViews: LinkedList<SurfaceView>) {

        val parentWlSurfaceResource = parentSurfaceView.wlSurfaceResource
        val parentWlSurface = parentWlSurfaceResource.implementation as WlSurface
        val parentSurface = parentWlSurface.surface

        parentSurface.siblings.forEach { sibling ->

            val siblingWlSurface = sibling.wlSurfaceResource.implementation as WlSurface
            val siblingSurface = siblingWlSurface.surface

            //only consider surface if it has a role.
            //TODO we could move the views to the generic role itf.
            if (siblingSurface.role.isPresent) {

                siblingSurface.views.forEach { siblingSurfaceView ->

                    if (siblingSurfaceView.parent.filter { siblingParentSurfaceView -> siblingParentSurfaceView == parentSurfaceView }.isPresent) {
                        addSiblingViews(siblingSurfaceView, surfaceViews)
                    }
                    else if (siblingSurfaceView == parentSurfaceView) {
                        surfaceViews.addFirst(siblingSurfaceView)
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
        views.forEach(Consumer<SurfaceView> { this.removeView(it) })
    }
}
