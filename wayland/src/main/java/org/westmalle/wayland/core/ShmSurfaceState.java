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

import com.google.auto.value.AutoValue;

import javax.annotation.Nonnegative;
import java.util.Optional;

@AutoValue
public abstract class ShmSurfaceState implements SurfaceRenderState {

    public static ShmSurfaceState create(@Nonnegative final int pitch,
                                         @Nonnegative final int height,
                                         final int target,
                                         final int shaderProgram,
                                         final int glFormat,
                                         final int glPixelType,
                                         final int texture) {
        return new AutoValue_ShmSurfaceState(pitch,
                                             height,
                                             target,
                                             shaderProgram,
                                             glFormat,
                                             glPixelType,
                                             texture);
    }

    @Nonnegative
    public abstract int getPitch();

    @Nonnegative
    public abstract int getHeight();

    public abstract int getTarget();

    public abstract int getShaderProgram();

    public abstract int getGlFormat();

    public abstract int getGlPixelType();

    public abstract int getTexture();


    @Override
    public Optional<SurfaceRenderState> accept(final SurfaceRenderStateVisitor surfaceRenderStateVisitor) {
        return surfaceRenderStateVisitor.visit(this);
    }
}
