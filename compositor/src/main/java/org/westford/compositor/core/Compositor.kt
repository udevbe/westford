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


import org.westford.compositor.protocol.WlOutput
import java.util.concurrent.TimeUnit
import javax.annotation.Nonnegative
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Compositor @Inject
internal constructor(private val renderPlatform: RenderPlatform) {

    fun requestRender() = this.renderPlatform.wlOutputs.forEach { this.render(it) }

    private fun render(wlOutput: WlOutput) = wlOutput.output.renderOutput.render(wlOutput)

    val time: Int @Nonnegative get() = TimeUnit.NANOSECONDS.toMillis(System.nanoTime()).toInt()
}