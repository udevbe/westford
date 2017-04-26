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
abstract class OutputMode {

    /**
     * @return bitfield of mode flags
     */
    abstract val flags: Int

    /**
     * @return width of the mode in hardware units
     */
    @get:Nonnegative
    abstract val width: Int

    /**
     * @return height of the mode in hardware units
     */
    @get:Nonnegative
    abstract val height: Int

    /**
     * @return vertical refresh rate in mHz
     */
    @get:Nonnegative
    abstract val refresh: Int

    abstract fun toBuilder(): Builder

    @AutoValue.Builder
    interface Builder {

        fun flags(flags: Int): Builder

        fun width(@Nonnegative width: Int): Builder

        fun height(@Nonnegative height: Int): Builder

        fun refresh(@Nonnegative refresh: Int): Builder

        fun build(): OutputMode
    }

    companion object {

        fun builder(): Builder {
            return AutoValue_OutputMode.Builder()
        }
    }
}
