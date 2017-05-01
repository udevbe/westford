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
import org.freedesktop.jaccall.Pointer
import org.westford.nativ.libpixman1.Libpixman1
import org.westford.nativ.libpixman1.pixman_box32
import org.westford.nativ.libpixman1.pixman_region32
import java.util.*

@AutoFactory(className = "PrivateFiniteRegionFactory",
             allowSubclasses = true) class FiniteRegion(@param:Provided private val libpixman1: Libpixman1,
                                                        @param:Provided private val finiteRegionFactory: FiniteRegionFactory,
                                                        val pixmanRegion32: Pointer<pixman_region32>) : Region {

    override fun hashCode(): Int {
        return Objects.hashCode(asList())
    }

    override fun asList(): List<Rectangle> {
        //int pointer
        val n_rects = Pointer.nref(0)
        val pixman_box32_array = Pointer.wrap<pixman_box32>(pixman_box32::class.java,
                                                            this.libpixman1.pixman_region32_rectangles(this.pixmanRegion32.address,
                                                                                                       n_rects.address))
        val size = n_rects.get()
        val boxes = ArrayList<Rectangle>(size)
        for (i in 0..size - 1) {
            val pixman_box32 = pixman_box32_array.get(i)
            val x = pixman_box32.x1()
            val y = pixman_box32.y1()

            val width = pixman_box32.x2() - x
            val height = pixman_box32.y2() - y
            boxes.add(Rectangle.create(x,
                                       y,
                                       width,
                                       height))
        }
        return boxes
    }

    //TODO unit test equals
    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (o !is Region) {
            return false
        }

        val region = o

        return region.asList().containsAll(asList()) && asList().containsAll(region.asList())
    }

    fun add(region: FiniteRegion) {
        this.libpixman1.pixman_region32_union(this.pixmanRegion32.address,
                                              this.pixmanRegion32.address,
                                              region.pixmanRegion32.address)
    }

    override fun add(rectangle: Rectangle) {
        this.libpixman1.pixman_region32_union_rect(this.pixmanRegion32.address,
                                                   this.pixmanRegion32.address,
                                                   rectangle.x,
                                                   rectangle.y,
                                                   rectangle.width,
                                                   rectangle.height)
    }

    override fun subtract(rectangle: Rectangle) {
        val delta_pixman_region32 = Pointer.ref(pixman_region32())
        this.libpixman1.pixman_region32_init_rect(delta_pixman_region32.address,
                                                  rectangle.x,
                                                  rectangle.y,
                                                  rectangle.width,
                                                  rectangle.height)
        this.libpixman1.pixman_region32_subtract(this.pixmanRegion32.address,
                                                 this.pixmanRegion32.address,
                                                 delta_pixman_region32.address)
        this.libpixman1.pixman_region32_fini(delta_pixman_region32.address)
    }

    override fun contains(point: Point): Boolean {
        return this.libpixman1.pixman_region32_contains_point(this.pixmanRegion32.address,
                                                              point.x,
                                                              point.y,
                                                              0L) != 0
    }

    override fun contains(clipping: Rectangle,
                          point: Point): Boolean {
        //fast path
        if (clipping.width == 0 && clipping.height == 0) {
            return false
        }
        this.libpixman1.pixman_region32_intersect_rect(this.pixmanRegion32.address,
                                                       this.pixmanRegion32.address,
                                                       clipping.x,
                                                       clipping.y,
                                                       clipping.width,
                                                       clipping.height)
        return this.libpixman1.pixman_region32_contains_point(this.pixmanRegion32.address,
                                                              point.x,
                                                              point.y,
                                                              0L) != 0
    }

    override fun contains(rectangle: Rectangle): Boolean {
        val pixman_box32 = pixman_box32()
        pixman_box32.x1(rectangle.x)
        pixman_box32.y1(rectangle.y)
        pixman_box32.x2(rectangle.x + rectangle.width)
        pixman_box32.y2(rectangle.y + rectangle.height)
        return Libpixman1.PIXMAN_REGION_OUT != this.libpixman1.pixman_region32_contains_rectangle(this.pixmanRegion32.address,
                                                                                                  Pointer.ref(pixman_box32).address)
    }

    override fun intersect(rectangle: Rectangle): Region {
        val region = this.finiteRegionFactory.create()

        this.libpixman1.pixman_region32_intersect_rect(region.pixmanRegion32.address,
                                                       this.pixmanRegion32.address,
                                                       rectangle.x,
                                                       rectangle.y,
                                                       rectangle.width,
                                                       rectangle.height)

        return region
    }

    override fun copy(): Region {
        val copyRegion = Pointer.ref(pixman_region32())
        this.libpixman1.pixman_region32_copy(copyRegion.address,
                                             this.pixmanRegion32.address)
        return this.finiteRegionFactory.create(copyRegion)
    }

    override fun isEmpty(): Boolean {
        return this.libpixman1.pixman_region32_not_empty(this.pixmanRegion32.address) == 0
    }

    fun finalize() {
        this.libpixman1.pixman_region32_fini(this.pixmanRegion32.address)
    }

    fun remove(region: FiniteRegion) {
        this.libpixman1.pixman_region32_subtract(this.pixmanRegion32.address,
                                                 this.pixmanRegion32.address,
                                                 region.pixmanRegion32.address)
    }

    fun clear() {
        this.libpixman1.pixman_region32_clear(this.pixmanRegion32.address)
    }
}
