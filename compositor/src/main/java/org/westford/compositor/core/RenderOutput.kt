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

/**
 * A [RenderPlatform] specific drawing output for a [Renderer].
 */
interface RenderOutput {

    /**
     * Request a render for this `RenderOutput` on the given wl output.

     * @param wlOutput a wayland output used as the rendering context.
     */
    fun render(wlOutput: WlOutput)

    /**
     * Disables any pending and future rendering for this connector.
     */
    open fun disable() {}

    /**
     * Enables rendering and triggers a redraw for this `RenderOutput`.

     * @param wlOutput a wayland output used as the rendering context.
     */
    open fun enable(wlOutput: WlOutput) {}


}
