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
package org.trinity.wayland.output.gl;

public enum GLBufferFormat {
    SHM_ARGB8888("surface",
                 "surface_argb8888"),
    SHM_XRGB8888("surface",
                 "surface_xrgb8888");

    private final String vertexShader;
    private final String fragmentShader;

    GLBufferFormat(final String vertexShader,
                   final String fragmentShader) {
        this.vertexShader = vertexShader;
        this.fragmentShader = fragmentShader;
    }

    public String getFragmentShader() {
        return this.fragmentShader;
    }

    public String getVertexShader() {
        return this.vertexShader;
    }
}
