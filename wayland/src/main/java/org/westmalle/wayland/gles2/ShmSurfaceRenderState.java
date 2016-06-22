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

import javax.annotation.Nonnegative;
import java.util.Optional;

@AutoValue
public abstract class ShmSurfaceRenderState implements SurfaceRenderState {

    public static ShmSurfaceRenderState create(@Nonnegative final int pitch,
                                               @Nonnegative final int height,
                                               final int target,
                                               final int shaderProgram,
                                               final int glFormat,
                                               final int glPixelType,
                                               final int texture) {
        return new AutoValue_ShmSurfaceRenderState(pitch,
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
