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

@AutoValue
abstract class OutputGeometry {

    /**
     * @return x position within the global compositor space
     */
    abstract val x: Int

    /**
     * @return y position within the global compositor space
     */
    abstract val y: Int

    /**
     * @return width in millimeters of the output
     */
    @get:Nonnegative
    abstract val physicalWidth: Int

    /**
     * @return height in millimeters of the output
     */
    @get:Nonnegative
    abstract val physicalHeight: Int

    /**
     * @return subpixel orientation of the output
     */
    abstract val subpixel: Int

    /**
     * @return textual description of the manufacturer
     */
    abstract val make: String

    /**
     * @return textual description of the model
     */
    abstract val model: String

    /**
     * @return transform that maps framebuffer to output
     */
    abstract val transform: Int

    abstract fun toBuilder(): Builder

    @AutoValue.Builder
    interface Builder {
        fun x(x: Int): Builder

        fun y(y: Int): Builder

        fun physicalWidth(@Nonnegative width: Int): Builder

        fun physicalHeight(@Nonnegative height: Int): Builder

        fun subpixel(subpixel: Int): Builder

        fun make(make: String): Builder

        fun model(model: String): Builder

        fun transform(transform: Int): Builder

        fun build(): OutputGeometry
    }

    companion object {

        fun builder(): Builder {
            return AutoValue_OutputGeometry.Builder()
        }
    }
}
