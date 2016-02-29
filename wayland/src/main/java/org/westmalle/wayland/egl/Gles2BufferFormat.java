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

import org.freedesktop.wayland.shared.WlShmFormat;

import javax.annotation.Nonnull;

public enum Gles2BufferFormat {
    SHM_ARGB8888(EglGles2Renderer.VERTEX_SHADER,
                 EglGles2Renderer.FRAGMENT_SHADER_ARGB8888,
                 WlShmFormat.ARGB8888.value);
    //TODO
//    SHM_XRGB8888(EglGles2Renderer.VERTEX_SHADER,
//                 EglGles2Renderer.FRAGMENT_SHADER_ARGB8888,
//                 WlShmFormat.XRGB8888.getValue());

    @Nonnull
    private final String vertexShader;
    @Nonnull
    private final String fragmentShader;
    private final int    wlShmFormat;

    Gles2BufferFormat(@Nonnull final String vertexShader,
                      @Nonnull final String fragmentShader,
                      final int wlShmFormat) {
        this.vertexShader = vertexShader;
        this.fragmentShader = fragmentShader;
        this.wlShmFormat = wlShmFormat;
    }

    @Nonnull
    public String getFragmentShaderSource() {
        return this.fragmentShader;
    }

    @Nonnull
    public String getVertexShaderSource() {
        return this.vertexShader;
    }

    public int getWlShmFormat() {
        return this.wlShmFormat;
    }
}
