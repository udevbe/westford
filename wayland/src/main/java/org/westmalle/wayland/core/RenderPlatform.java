/*
 * Westmalle Wayland Compositor.
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
package org.westmalle.wayland.core;

import org.westmalle.Signal;
import org.westmalle.Slot;
import org.westmalle.wayland.core.events.RenderOutputDestroyed;
import org.westmalle.wayland.core.events.RenderOutputNew;

import javax.annotation.Nonnull;
import java.util.List;

public interface RenderPlatform {

    @Nonnull
    List<? extends RenderOutput> getRenderOutputs();

    Signal<RenderOutputNew, Slot<RenderOutputNew>> getRenderOutputNewSignal();

    Signal<RenderOutputDestroyed, Slot<RenderOutputDestroyed>> getRenderOutputDestroyedSignal();
}
