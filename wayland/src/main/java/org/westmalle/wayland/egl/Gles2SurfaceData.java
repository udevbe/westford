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

import com.sun.jna.Memory;
import com.sun.jna.Native;
import org.freedesktop.wayland.server.ShmBuffer;
import org.westmalle.wayland.nativ.LibGLESv2;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;

import static org.westmalle.wayland.nativ.LibGLESv2.*;

public class Gles2SurfaceData {

    public static Gles2SurfaceData create(@Nonnull final LibGLESv2 libGLESv2) {
        final Memory tex = new Memory(Integer.BYTES);
        libGLESv2.glGenTextures(1,
                                tex);
        return new Gles2SurfaceData(tex);
    }

    private final Memory tex;
    private       int    width;
    private       int    height;

    private Gles2SurfaceData(final Memory tex) {
        this.tex = tex;
    }

    public void init(@Nonnull final LibGLESv2 libGLESv2,
                     final ShmBuffer buffer) {
        this.width = buffer.getStride() / Integer.BYTES;
        this.height = buffer.getHeight();
        final ByteBuffer pixels = buffer.getData();

        libGLESv2.glBindTexture(GL_TEXTURE_2D,
                                getTexture().getInt(0));
        libGLESv2.glTexImage2D(GL_TEXTURE_2D,
                               0,
                               GL_RGBA,
                               this.width,
                               this.height,
                               0,
                               GL_RGBA,
                               GL_UNSIGNED_BYTE,
                               Native.getDirectBufferPointer(pixels));

        libGLESv2.glTexParameteri(GL_TEXTURE_2D,
                                  GL_TEXTURE_WRAP_S,
                                  GL_CLAMP_TO_EDGE);
        libGLESv2.glTexParameteri(GL_TEXTURE_2D,
                                  GL_TEXTURE_WRAP_T,
                                  GL_CLAMP_TO_EDGE);
        libGLESv2.glTexParameteri(GL_TEXTURE_2D,
                                  GL_TEXTURE_MIN_FILTER,
                                  GL_NEAREST);
        libGLESv2.glTexParameteri(GL_TEXTURE_2D,
                                  GL_TEXTURE_MAG_FILTER,
                                  GL_NEAREST);
    }

    public void makeActive(@Nonnull final LibGLESv2 libGLESv2,
                           @Nonnull final ShmBuffer buffer) {

        this.width = buffer.getStride() / 4;
        this.height = buffer.getHeight();
        final ByteBuffer pixels = buffer.getData();

        libGLESv2.glBindTexture(GL_TEXTURE_2D,
                                getTexture().getInt(0));
        libGLESv2.glTexSubImage2D(GL_TEXTURE_2D,
                                  0,
                                  0,
                                  0,
                                  this.width,
                                  this.height,
                                  GL_RGBA,
                                  GL_UNSIGNED_BYTE,
                                  Native.getDirectBufferPointer(pixels));
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    private Memory getTexture() {
        return this.tex;
    }

    public void destroy(@Nonnull final LibGLESv2 libGLESv2) {
        libGLESv2.glDeleteTextures(1,
                                   getTexture());
    }
}
