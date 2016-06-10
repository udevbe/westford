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
package org.westmalle.wayland.x11;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import org.westmalle.wayland.core.EglPlatform;
import org.westmalle.wayland.core.Renderer;
import org.westmalle.wayland.nativ.libEGL.LibEGL;
import org.westmalle.wayland.protocol.WlOutput;

import javax.annotation.Nonnull;
import java.util.List;

@AutoFactory(className = "PrivateX11EglPlatformFactory",
             allowSubclasses = true)
public class X11EglPlatform implements EglPlatform {

    @Nonnull
    private final LibEGL      libEGL;
    @Nonnull
    private final X11Platform x11Platform;
    private final long        eglDisplay;
    private final long        eglSurface;
    private final long        eglContext;

    X11EglPlatform(@Provided @Nonnull final LibEGL libEGL,
                   @Nonnull
                   final X11Platform x11Platform,
                   final long eglDisplay,
                   final long eglSurface,
                   final long eglContext) {
        this.libEGL = libEGL;
        this.x11Platform = x11Platform;
        this.eglDisplay = eglDisplay;
        this.eglSurface = eglSurface;
        this.eglContext = eglContext;
    }

    @Override
    public void end() {
        this.libEGL.eglSwapBuffers(this.eglDisplay,
                                   this.eglSurface);
    }

    @Override
    public long getEglDisplay() {
        return this.eglDisplay;
    }

    @Override
    public long getEglSurface() {
        return this.eglSurface;
    }

    @Override
    public long getEglContext() {
        return this.eglContext;
    }

    @Override
    public List<WlOutput> getWlOutputs() {
        return this.x11Platform.getWlOutputs();
    }

    @Override
    public void accept(final Renderer renderer) {
        renderer.visit(this);
    }

    @Nonnull
    public X11Platform getX11Platform() {
        return x11Platform;
    }
}
