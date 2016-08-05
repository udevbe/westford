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
package org.westmalle.wayland.gles2;

import com.google.auto.value.AutoValue;
import org.freedesktop.jaccall.Pointer;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.Optional;

@AutoValue
public abstract class EglSurfaceRenderState implements SurfaceRenderState {

    public static EglSurfaceRenderState create(@Nonnegative final int pitch,
                                               @Nonnegative final int height,
                                               final int target,
                                               final int shaderProgram,
                                               final boolean yInverted,
                                               final int[] textures,
                                               final long[] eglImages) {
        return new AutoValue_EglSurfaceRenderState(pitch,
                                                   height,
                                                   target,
                                                   shaderProgram,
                                                   yInverted,
                                                   textures,
                                                   eglImages);
    }

    @Nonnegative
    public abstract int getPitch();

    @Nonnegative
    public abstract int getHeight();

    public abstract int getTarget();

    public abstract int getShaderProgram();

    public abstract boolean getYInverted();

    @SuppressWarnings("mutable")
    public abstract int[] getTextures();

    @SuppressWarnings("mutable")
    public abstract long[] getEglImages();

    @Override
    public Optional<SurfaceRenderState> accept(final SurfaceRenderStateVisitor surfaceRenderStateVisitor) {
        return surfaceRenderStateVisitor.visit(this);
    }
}
