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

import javax.annotation.Nonnegative

data class Rectangle(val x: Int,
                     val y: Int,
                     @param:Nonnegative val width: Int,
                     @param:Nonnegative val height: Int) {

    constructor(position: Point,
                width: Int,
                height: Int) : this(position.x,
                                    position.y,
                                    width,
                                    height)

    val position: Point
        get() = Point(x,
                      y)

    companion object {

        val ZERO = Rectangle(0,
                             0,
                             0,
                             0)

        fun create(a: Point,
                   b: Point): Rectangle {

            val width: Int
            val x: Int
            if (a.x > b.x) {
                width = a.x - b.x
                x = b.x
            }
            else {
                width = b.x - a.x
                x = a.x
            }

            val height: Int
            val y: Int
            if (a.x > b.y) {
                height = a.y - b.y
                y = b.y
            }
            else {
                height = b.y - a.y
                y = a.y
            }

            return Rectangle(x,
                             y,
                             width,
                             height)
        }
    }
}
