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
import com.google.auto.factory.Provided
import org.freedesktop.wayland.server.WlOutputResource
import org.freedesktop.wayland.shared.WlOutputTransform
import org.westford.Signal
import org.westford.compositor.core.calc.Mat4
import org.westford.compositor.core.events.OutputTransform

import javax.annotation.Nonnegative

@AutoFactory(className = "PrivateOutputFactory",
             allowSubclasses = true) class Output(@param:Provided val regionFactory: FiniteRegionFactory,
                                                  @param:Provided var region: FiniteRegion,
                                                  val renderOutput: RenderOutput,
                                                  val name: String) {

    val transformSignal = Signal<OutputTransform>()
    val modeSignal = Signal<OutputMode>()

    @Nonnegative @get:Nonnegative var scale = 1f
        set(@Nonnegative scale) {
            field = scale
            updateOutputTransform()
        }
    /**
     * translate from output space to compositor space

     * @return
     */
    var transform = Mat4.IDENTITY
        private set
    /**
     * translate from compositor space to output space

     * @return
     */
    var inverseTransform = Mat4.IDENTITY
        private set
    var geometry = OutputGeometry(physicalWidth = 0,
                                  physicalHeight = 0,
                                  make = "",
                                  model = "",
                                  x = 0,
                                  y = 0,
                                  subpixel = 0,
                                  transform = 0)
        private set
    var mode = OutputMode(width = 0,
                          height = 0,
                          refresh = 0,
                          flags = 0)
        private set

    fun update(resources: Set<WlOutputResource>,
               outputGeometry: OutputGeometry) {
        if (this.geometry != outputGeometry) {
            this.geometry = outputGeometry
            updateOutputTransform()
        }

        resources.forEach { this.notifyGeometry(it) }
    }

    private fun updateOutputTransform() {

        val scaleMat = Transforms.SCALE(this.scale)
        val x = this.geometry.x
        val y = this.geometry.y
        val moveMat = Transforms.TRANSLATE(x,
                                           y)
        val transformMat: Mat4
        val transformNr = this.geometry.transform
        if (transformNr == WlOutputTransform.NORMAL.value) {
            transformMat = Mat4.IDENTITY
        }
        else if (transformNr == WlOutputTransform._90.value) {
            transformMat = Transforms._90
        }
        else if (transformNr == WlOutputTransform._180.value) {
            transformMat = Transforms._180
        }
        else if (transformNr == WlOutputTransform._270.value) {
            transformMat = Transforms._270
        }
        else if (transformNr == WlOutputTransform.FLIPPED.value) {
            transformMat = Transforms.FLIPPED
        }
        else if (transformNr == WlOutputTransform.FLIPPED_90.value) {
            transformMat = Transforms.FLIPPED_90
        }
        else if (transformNr == WlOutputTransform.FLIPPED_180.value) {
            transformMat = Transforms.FLIPPED_180
        }
        else if (transformNr == WlOutputTransform.FLIPPED_270.value) {
            transformMat = Transforms.FLIPPED_270
        }
        else {
            transformMat = Mat4.IDENTITY
        }

        val newTransform = transformMat * scaleMat * moveMat
        if (this.transform != newTransform) {
            this.transform = newTransform
            this.inverseTransform = this.transform.invert()
            updateRegion()
            this.transformSignal.emit(OutputTransform())
        }
    }

    private fun updateRegion() {
        val regionTopLeft = this.transform * Point.ZERO
        val regionBottomRight = this.transform * Point(this.mode.width,
                                                       this.mode.height)
        //TODO fire region event?
        //TODO check if the region is properly updated in the unit tests
        this.region = this.regionFactory.create() + Rectangle.create(regionTopLeft,
                                                                     regionBottomRight)
    }

    fun update(resources: Set<WlOutputResource>,
               outputMode: OutputMode): Output {
        if (this.mode != outputMode) {
            this.mode = outputMode
            updateRegion()
            this.modeSignal.emit(outputMode)
        }
        resources.forEach { this.notifyMode(it) }
        return this
    }

    fun notifyMode(wlOutputResource: WlOutputResource) {
        wlOutputResource.mode(this.mode.flags,
                              this.mode.width,
                              this.mode.height,
                              this.mode.refresh)
    }

    fun notifyGeometry(wlOutputResource: WlOutputResource) {
        wlOutputResource.geometry(this.geometry.x,
                                  this.geometry.y,
                                  this.geometry.physicalWidth,
                                  this.geometry.physicalHeight,
                                  this.geometry.subpixel,
                                  this.geometry.make,
                                  this.geometry.model,
                                  this.geometry.transform)
    }

    fun local(global: Point): Point = this.inverseTransform * global

    fun global(outputLocal: Point): Point = this.transform * outputLocal
}
