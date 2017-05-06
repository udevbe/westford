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
package org.westford.compositor.core.calc

import org.westford.compositor.core.Point

import javax.annotation.Nonnegative

data class Vec4(val x: Float,
                val y: Float,
                val z: Float,
                val w: Float) {

    constructor(array: FloatArray) : this(array[0],
                                          array[1],
                                          array[2],
                                          array[3])

    constructor(array: FloatArray,
                @Nonnegative offset: Int) : this(array[offset],
                                                 array[1 + offset],
                                                 array[2 + offset],
                                                 array[3 + offset])

    operator fun plus(right: Vec4): Vec4 = Vec4(x + right.x,
                                                y + right.y,
                                                z + right.z,
                                                w + right.w)

    operator fun minus(right: Vec4): Vec4 = Vec4(x - right.x,
                                                 y - right.y,
                                                 z - right.z,
                                                 w - right.w)

    fun toPoint(): Point = Point(x.toInt(),
                                 y.toInt())
}
