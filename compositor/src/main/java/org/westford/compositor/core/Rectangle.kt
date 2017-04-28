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

import com.google.auto.value.AutoValue

import javax.annotation.Nonnegative

@AutoValue abstract class Rectangle {

    @get:Nonnegative abstract val width: Int

    @get:Nonnegative abstract val height: Int

    val position: Point
        get() = Point.create(x,
                             y)

    abstract val x: Int

    abstract val y: Int

    abstract fun toBuilder(): Builder

    @AutoValue.Builder interface Builder {
        fun x(x: Int): Builder

        fun y(y: Int): Builder

        fun width(@Nonnegative width: Int): Builder

        fun height(@Nonnegative height: Int): Builder

        fun build(): Rectangle
    }

    companion object {

        val ZERO = builder().build()

        fun create(position: Point,
                   @Nonnegative width: Int,
                   @Nonnegative height: Int): Rectangle {
            return create(position.x,
                          position.y,
                          width,
                          height)
        }

        fun create(x: Int,
                   y: Int,
                   @Nonnegative width: Int,
                   @Nonnegative height: Int): Rectangle {
            return builder().x(x).y(y).width(width).height(height).build()
        }

        fun builder(): Builder {
            return AutoValue_Rectangle.Builder().x(0).y(0).width(0).height(0)
        }

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

            return create(x,
                          y,
                          width,
                          height)
        }
    }
}
