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
package org.westmalle.wayland.output.gl;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureData;
import org.freedesktop.wayland.server.ShmBuffer;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;

public class GLSurfaceData {

    @Nonnull
    public static GLSurfaceData create(@Nonnull final GL2ES2 gl,
                                       @Nonnull final ShmBuffer buffer) {
        final Texture texture = new Texture(gl,
                                            createTextureData(gl,
                                                              buffer));
        texture.bind(gl);
        texture.setTexParameteri(gl,
                                 GL.GL_TEXTURE_WRAP_S,
                                 GL.GL_CLAMP_TO_EDGE);
        texture.setTexParameteri(gl,
                                 GL.GL_TEXTURE_WRAP_T,
                                 GL.GL_CLAMP_TO_EDGE);
        texture.setTexParameteri(gl,
                                 GL.GL_TEXTURE_MIN_FILTER,
                                 GL.GL_NEAREST);
        texture.setTexParameteri(gl,
                                 GL.GL_TEXTURE_MAG_FILTER,
                                 GL.GL_NEAREST);

        return new GLSurfaceData(texture);
    }

    private static TextureData createTextureData(final GL2ES2 gl,
                                                 final ShmBuffer buffer) {
        final int        width  = buffer.getStride() / 4;
        final int        height = buffer.getHeight();
        final ByteBuffer pixels = buffer.getData();

        return new TextureData(gl.getGLProfile(),
                               GL.GL_RGBA,
                               width,
                               height,
                               0,
                               GL.GL_RGBA,
                               GL.GL_UNSIGNED_BYTE,
                               false,
                               false,
                               false,
                               pixels,
                               null);
    }

    private final Texture texture;

    private GLSurfaceData(final Texture texture) {
        this.texture = texture;
    }

    @Nonnull
    public Texture getTexture() {
        return this.texture;
    }

    public void destroy(@Nonnull final GL2ES2 gl) {
        this.texture.destroy(gl);
    }

    public void update(@Nonnull final GL2ES2 gl,
                       @Nonnull final ShmBuffer buffer) {
        getTexture().updateSubImage(gl,
                                    createTextureData(gl,
                                                      buffer),
                                    0,
                                    0,
                                    0);
    }
}
