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

@AutoValue abstract class ShmSurfaceState : SurfaceRenderState {

    @get:Nonnegative abstract val pitch: Int

    @get:Nonnegative abstract val height: Int

    abstract val target: Int

    abstract val shaderProgram: Int

    abstract val glFormat: Int

    abstract val glPixelType: Int

    abstract val texture: Int

    override fun accept(surfaceRenderStateVisitor: SurfaceRenderStateVisitor): SurfaceRenderState? {
        return surfaceRenderStateVisitor.visit(this)
    }

    companion object {

        fun create(@Nonnegative pitch: Int,
                   @Nonnegative height: Int,
                   target: Int,
                   shaderProgram: Int,
                   glFormat: Int,
                   glPixelType: Int,
                   texture: Int): ShmSurfaceState {
            return AutoValue_ShmSurfaceState(pitch,
                                             height,
                                             target,
                                             shaderProgram,
                                             glFormat,
                                             glPixelType,
                                             texture)
        }
    }
}
