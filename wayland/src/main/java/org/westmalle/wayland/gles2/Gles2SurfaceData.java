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
package org.westmalle.wayland.gles2;

import org.freedesktop.jaccall.JNI;
import org.freedesktop.jaccall.Pointer;
import org.freedesktop.wayland.server.ShmBuffer;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.westmalle.wayland.core.Surface;
import org.westmalle.wayland.nativ.libGLESv2.LibGLESv2;
import org.westmalle.wayland.protocol.WlSurface;

import javax.annotation.Nonnull;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_BGRA_EXT;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_NEAREST;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_TEXTURE_2D;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_TEXTURE_MAG_FILTER;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_TEXTURE_MIN_FILTER;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_UNSIGNED_BYTE;

public class Gles2SurfaceData {

    private final Pointer<Integer> textureId;
    private final int              width;
    private final int              height;

    private Gles2SurfaceData(final Pointer<Integer> textureId,
                             final int width,
                             final int height) {
        this.textureId = textureId;
        this.width = width;
        this.height = height;
    }

    public static Gles2SurfaceData create(@Nonnull final LibGLESv2 libGLESv2,
                                          @Nonnull final ShmBuffer shmBuffer) {

        final int bufferWidth  = shmBuffer.getStride() / Integer.BYTES;
        final int bufferHeight = shmBuffer.getHeight();

        final Pointer<Integer> textureId = Pointer.nref(0);
        libGLESv2.glGenTextures(1,
                                textureId.address);

        //upload buffer to gpu
        libGLESv2.glBindTexture(GL_TEXTURE_2D,
                                textureId.dref());
        libGLESv2.glTexParameteri(GL_TEXTURE_2D,
                                  GL_TEXTURE_MIN_FILTER,
                                  GL_NEAREST);
        libGLESv2.glTexParameteri(GL_TEXTURE_2D,
                                  GL_TEXTURE_MAG_FILTER,
                                  GL_NEAREST);
        libGLESv2.glTexImage2D(GL_TEXTURE_2D,
                               0,
                               GL_BGRA_EXT /*glesv2 doesnt care what internal format we give it, it must however match the external format*/,
                               bufferWidth,
                               bufferHeight,
                               0,
                               GL_BGRA_EXT,
                               GL_UNSIGNED_BYTE,
                               0L);

        return new Gles2SurfaceData(textureId,
                                    bufferWidth,
                                    bufferHeight);
    }

    public void update(@Nonnull final LibGLESv2 libGLESv2,
                       @Nonnull final WlSurfaceResource wlSurfaceResource,
                       @Nonnull final ShmBuffer shmBuffer) {

        final WlSurface wlSurface = (WlSurface) wlSurfaceResource.getImplementation();
        final Surface   surface   = wlSurface.getSurface();

        final int bufferWidth  = shmBuffer.getStride() / Integer.BYTES;
        final int bufferHeight = shmBuffer.getHeight();

        libGLESv2.glBindTexture(GL_TEXTURE_2D,
                                this.textureId.dref());

        //TODO use damage hints from surface & glTexSubImage2D
        shmBuffer.beginAccess();
        libGLESv2.glTexImage2D(GL_TEXTURE_2D,
                               0,
                               GL_BGRA_EXT /*glesv2 doesnt care what internal format we give it, it must however match the external format*/,
                               bufferWidth,
                               bufferHeight,
                               0,
                               GL_BGRA_EXT,
                               GL_UNSIGNED_BYTE,
                               JNI.unwrap(shmBuffer.getData()));
        shmBuffer.endAccess();
        surface.firePaintCallbacks((int) NANOSECONDS.toMillis(System.nanoTime()));
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public void delete(@Nonnull final LibGLESv2 libGLESv2) {
        libGLESv2.glDeleteTextures(1,
                                   this.textureId.address);
    }
}
