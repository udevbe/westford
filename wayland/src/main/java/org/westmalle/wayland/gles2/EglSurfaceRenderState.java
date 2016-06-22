//Copyright 2016 Erik De Rijcke
//
//Licensed under the Apache License,Version2.0(the"License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
//http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing,software
//distributed under the License is distributed on an"AS IS"BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
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

    public abstract int[] getTextures();

    public abstract long[] getEglImages();

    @Override
    public Optional<SurfaceRenderState> accept(final SurfaceRenderStateVisitor surfaceRenderStateVisitor) {
        return surfaceRenderStateVisitor.visit(this);
    }
}
