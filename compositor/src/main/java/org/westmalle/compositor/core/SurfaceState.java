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
package org.westmalle.compositor.core;

import com.google.auto.value.AutoValue;
import org.freedesktop.wayland.server.WlBufferResource;
import org.westmalle.compositor.core.AutoValue_SurfaceState;
import org.westmalle.compositor.core.calc.Mat4;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.Optional;

@AutoValue
public abstract class SurfaceState {

    static Builder builder() {
        return new AutoValue_SurfaceState.Builder().opaqueRegion(Optional.<Region>empty())
                                                   .inputRegion(Optional.<Region>empty())
                                                   .damage(Optional.<Region>empty())
                                                   .buffer(Optional.<WlBufferResource>empty())
                                                   .bufferTransform(Mat4.IDENTITY)
                                                   .positionTransform(Mat4.IDENTITY)
                                                   .scale(1);
    }

    @Nonnull
    public abstract Optional<Region> getOpaqueRegion();

    @Nonnull
    public abstract Optional<Region> getInputRegion();

    @Nonnull
    public abstract Optional<Region> getDamage();

    @Nonnull
    public abstract Optional<WlBufferResource> getBuffer();

    @Nonnull
    public abstract Mat4 getBufferTransform();

    @Nonnull
    public abstract Mat4 getPositionTransform();

    @Nonnegative
    public abstract int getScale();

    public abstract Builder toBuilder();

    @AutoValue.Builder
    interface Builder {
        Builder opaqueRegion(Optional<Region> wlRegionResource);

        Builder inputRegion(Optional<Region> wlRegionResource);

        Builder damage(Optional<Region> damage);

        Builder buffer(Optional<WlBufferResource> wlBufferResource);

        Builder bufferTransform(Mat4 bufferTransform);

        Builder positionTransform(Mat4 positionTransform);

        Builder scale(int scale);

        SurfaceState build();
    }
}