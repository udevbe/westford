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

import com.google.auto.value.AutoValue
import org.westford.compositor.core.Point

import javax.annotation.Nonnegative

@AutoValue
abstract class Vec4 {

    abstract fun toBuilder(): Builder

    fun add(right: Vec4): Vec4 {
        return Vec4.create(x + right.x,
                y + right.y,
                z + right.z,
                w + right.w)
    }

    abstract val x: Float

    abstract val y: Float

    abstract val z: Float

    abstract val w: Float

    fun subtract(right: Vec4): Vec4 {
        return Vec4.create(x - right.x,
                y - right.y,
                z - right.z,
                w - right.w)
    }

    fun toPoint(): Point {
        return Point.create(x.toInt(),
                y.toInt())
    }

    @AutoValue.Builder
    interface Builder {

        fun x(x: Float): Builder

        fun y(y: Float): Builder

        fun z(z: Float): Builder

        fun w(z: Float): Builder

        fun build(): Vec4
    }

    companion object {

        fun create(array: FloatArray): Vec4 {
            return Vec4.create(array,
                    0)
        }

        fun create(array: FloatArray,
                   @Nonnegative offset: Int): Vec4 {
            if (array.size < 4) {
                throw IllegalArgumentException("Array length must be >= 4")
            }
            return Vec4.builder()
                    .x(array[offset])
                    .y(array[1 + offset])
                    .z(array[2 + offset])
                    .w(array[3 + offset])
                    .build()
        }

        fun builder(): Builder {
            return AutoValue_Vec4.Builder().x(0)
                    .y(0)
                    .z(0)
                    .w(0)
        }

        fun create(x: Float,
                   y: Float,
                   z: Float,
                   w: Float): Vec4 {
            return Vec4.builder()
                    .x(x)
                    .y(y)
                    .z(z)
                    .w(w)
                    .build()
        }
    }
}
