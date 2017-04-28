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
import org.westford.compositor.core.calc.Vec4

@AutoValue abstract class Point {

    fun toVec4(): Vec4 {
        return Vec4.create(x.toFloat(),
                           y.toFloat(),
                           0f,
                           1f)
    }

    abstract val x: Int

    abstract val y: Int

    fun add(right: Point): Point {
        return Point.create(x + right.x,
                            y + right.y)
    }

    fun subtract(right: Point): Point {
        return Point.create(x - right.x,
                            y - right.y)
    }

    abstract fun toBuilder(): Builder

    @AutoValue.Builder interface Builder {
        fun x(x: Int): Builder

        fun y(y: Int): Builder

        fun build(): Point
    }

    companion object {

        val ZERO = builder().build()

        fun create(x: Int,
                   y: Int): Point {
            return builder().x(x).y(y).build()
        }

        fun builder(): Builder {
            return AutoValue_Point.Builder().x(0).y(0)
        }
    }
}
