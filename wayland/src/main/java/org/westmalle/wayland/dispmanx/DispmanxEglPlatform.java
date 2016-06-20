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
package org.westmalle.wayland.dispmanx;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import org.westmalle.wayland.core.EglPlatform;
import org.westmalle.wayland.core.Platform;
import org.westmalle.wayland.core.Renderer;
import org.westmalle.wayland.nativ.libEGL.LibEGL;
import org.westmalle.wayland.protocol.WlOutput;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

@AutoFactory(className = "PrivateDispmanxEglPlatformFactory",
             allowSubclasses = true)
//TODO unit tests
public class DispmanxEglPlatform implements EglPlatform {

    @Nonnull
    private final LibEGL           libEGL;
    @Nonnull
    private final DispmanxPlatform dispmanxPlatform;
    private final long             eglDisplay;
    private final long             eglSurface;
    private final long             eglContext;
    @Nonnull
    private final String           eglExtensions;

    DispmanxEglPlatform(@Provided @Nonnull final LibEGL libEGL,
                        @Nonnull final DispmanxPlatform dispmanxPlatform,
                        final long eglDisplay,
                        final long eglSurface,
                        final long eglContext,
                        @Nonnull final String eglExtensions) {
        this.libEGL = libEGL;
        this.dispmanxPlatform = dispmanxPlatform;
        this.eglDisplay = eglDisplay;
        this.eglSurface = eglSurface;
        this.eglContext = eglContext;
        this.eglExtensions = eglExtensions;
    }

    @Override
    public void begin() {
        this.libEGL.eglMakeCurrent(this.eglDisplay,
                                   this.eglSurface,
                                   this.eglSurface,
                                   this.eglContext);
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

    @Nonnull
    @Override
    public WlOutput getWlOutput() {
        return this.dispmanxPlatform.getWlOutput();
    }

    @Override
    public void accept(@Nonnull final Renderer renderer) {
        renderer.visit(this);
    }

    @Nonnull
    public DispmanxPlatform getDispmanxPlatform() {
        return this.dispmanxPlatform;
    }

    @Nonnull
    @Override
    public String getEglExtensions() {
        return this.eglExtensions;
    }
}
