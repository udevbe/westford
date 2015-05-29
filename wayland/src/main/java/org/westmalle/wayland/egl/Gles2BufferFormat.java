//Copyright 2015 Erik De Rijcke
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
package org.westmalle.wayland.egl;

import javax.annotation.Nonnull;

public enum Gles2BufferFormat {
    SHM_ARGB8888("surface",
                 "surface_argb8888"),
    SHM_XRGB8888("surface",
                 "surface_xrgb8888");

    @Nonnull
    private final String vertexShader;
    @Nonnull
    private final String fragmentShader;

    Gles2BufferFormat(@Nonnull final String vertexShader,
                      @Nonnull final String fragmentShader) {
        this.vertexShader = vertexShader;
        this.fragmentShader = fragmentShader;
    }

    @Nonnull
    public String getFragmentShader() {
        return this.fragmentShader;
    }

    @Nonnull
    public String getVertexShader() {
        return this.vertexShader;
    }
}
