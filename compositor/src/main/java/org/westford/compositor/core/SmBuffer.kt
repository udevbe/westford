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
import org.freedesktop.wayland.server.ShmBuffer
import org.freedesktop.wayland.server.WlBufferResource

import javax.annotation.Nonnegative

@AutoValue
abstract class SmBuffer : Buffer {

    override fun accept(bufferVisitor: BufferVisitor) {
        bufferVisitor.visit(this)
    }

    @get:Nonnegative
    abstract override val width: Int

    @get:Nonnegative
    abstract override val height: Int

    abstract override val wlBufferResource: WlBufferResource

    abstract val shmBuffer: ShmBuffer

    companion object {

        fun create(@Nonnegative width: Int,
                   @Nonnegative height: Int,
                   wlBufferResource: WlBufferResource,
                   shmBuffer: ShmBuffer): SmBuffer {
            return AutoValue_SmBuffer(width,
                    height,
                    wlBufferResource,
                    shmBuffer)
        }
    }
}
