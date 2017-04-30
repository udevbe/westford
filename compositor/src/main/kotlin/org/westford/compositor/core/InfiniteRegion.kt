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

import javax.inject.Inject
import javax.inject.Singleton

/**
 * x: -32768
 * y: -32768
 * width: 0x7fffffff
 * height: 0x7fffffff
 */
@Singleton class InfiniteRegion @Inject internal constructor(private val finiteRegionFactory: FiniteRegionFactory) : Region {

    override fun asList(): List<Rectangle> {
        return INFINITE_RECT
    }

    override fun add(rectangle: Rectangle) {}

    override fun subtract(rectangle: Rectangle) {}

    override fun contains(point: Point) = true

    override fun contains(clipping: Rectangle,
                          point: Point): Boolean {
        val finiteRegion = this.finiteRegionFactory.create()
        finiteRegion.add(clipping)
        return finiteRegion.contains(point)
    }

    override fun contains(rectangle: Rectangle) = true

    override fun intersect(rectangle: Rectangle) = this

    override fun copy() = this

    override fun isEmpty() = false

    companion object {
        private val INFINITE_RECT = listOf<Rectangle>(Rectangle.create(java.lang.Short.MIN_VALUE.toInt(),
                                                                       java.lang.Short.MIN_VALUE.toInt(),
                                                                       Integer.MAX_VALUE,
                                                                       Integer.MAX_VALUE))
    }

    //TODO equals & hash?
}
