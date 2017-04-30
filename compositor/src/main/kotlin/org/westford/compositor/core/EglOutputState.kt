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
import org.westford.compositor.core.calc.Mat4

@AutoValue abstract class EglOutputState {
    //TODO damage

    abstract val glTransform: Mat4

    abstract fun toBuilder(): Builder

    @AutoValue.Builder interface Builder {

        fun glTransform(glTransform: Mat4): Builder

        fun build(): EglOutputState
    }

    companion object {

        fun builder(): Builder {
            return AutoValue_EglOutputState.Builder()
        }
    }
}
