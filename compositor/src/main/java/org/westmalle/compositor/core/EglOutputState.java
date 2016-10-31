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
import org.westmalle.compositor.core.AutoValue_EglOutputState;
import org.westmalle.compositor.core.calc.Mat4;

import javax.annotation.Nonnull;

@AutoValue
public abstract class EglOutputState {

    public static Builder builder() {
        return new AutoValue_EglOutputState.Builder();
    }
    //TODO damage

    @Nonnull
    public abstract Mat4 getGlTransform();

    public abstract Builder toBuilder();

    @AutoValue.Builder
    public interface Builder {

        Builder glTransform(Mat4 glTransform);

        EglOutputState build();
    }
}
