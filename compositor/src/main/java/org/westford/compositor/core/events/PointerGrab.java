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
package org.westford.compositor.core.events;

import com.google.auto.value.AutoValue;
import org.westford.compositor.core.SurfaceView;

import java.util.Optional;

@AutoValue
public abstract class PointerGrab {
    public static PointerGrab create(final Optional<SurfaceView> surfaceView) {
        return new AutoValue_PointerGrab(surfaceView);
    }

    public abstract Optional<SurfaceView> getWlSurfaceResource();
}
