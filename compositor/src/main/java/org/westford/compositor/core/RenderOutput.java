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
package org.westford.compositor.core;

import org.westford.compositor.protocol.WlOutput;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * A {@link RenderPlatform} specific drawing output for a {@link Renderer}.
 */
public interface RenderOutput {

    /**
     * Request a render for this {@code RenderOutput} on the given wl output.
     *
     * @param wlOutput a wayland output used as the rendering context.
     */
    void render(WlOutput wlOutput);

    /**
     * Called by the @{@link Renderer} of this {@code RenderOutput} when it starts to draw to it's back buffer.
     * <p>
     * A connector implementation can use this hook to perform any pre drawing actions.
     * </p>
     */
    default void renderBegin() {}

    /**
     * Called by the @{@link Renderer} of this {@code RenderOutput} when it has finished drawing to it's back buffer.
     * <p>
     * A connector implementation can use this hook to perform any post back buffer drawing actions.
     * </p>
     */
    default void renderEndBeforeSwap() {}

    /**
     * Called by the @{@link Renderer} of this {@code RenderOutput} when it has swapped the back buffer and front buffer.
     * <p>
     * A connector implementation can use this hook to perform any post front/back buffer swapping actions.
     * </p>
     */
    default void renderEndAfterSwap() {}

    /**
     * Disables any pending and future rendering for this connector.
     */
    default void disable() {}

    /**
     * Enables rendering and triggers a redraw for this {@code RenderOutput}.
     *
     * @param wlOutput a wayland output used as the rendering context.
     */
    default void enable(@Nonnull final WlOutput wlOutput) {}


}
