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

import org.westford.compositor.core.calc.Vec4

data class Point(val x: Int = 0,
                 val y: Int = 0) {

    fun toVec4(): Vec4 {
        return Vec4(x.toFloat(),
                    y.toFloat(),
                    0f,
                    1f)
    }

    operator fun plus(right: Point): Point {
        return Point(x + right.x,
                     y + right.y)
    }

    operator fun minus(right: Point): Point {
        return Point(x - right.x,
                     y - right.y)
    }

    companion object {
        val ZERO: Point = Point(0,
                                0)
    }
}
