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
import com.sun.jna.Pointer;
import org.westmalle.wayland.core.RenderOutput;
import org.westmalle.wayland.nativ.libEGL.LibEGL;

import javax.annotation.Nonnull;

@AutoFactory(className = "PrivateX11EglOutputFactory",
             allowSubclasses = true)
public class X11EglOutput implements RenderOutput {

    @Nonnull
    private final LibEGL  libEGL;
    @Nonnull
    private final Pointer eglDisplay;
    @Nonnull
    private final Pointer eglSurface;
    @Nonnull
    private final Pointer eglContext;

    X11EglOutput(@Provided @Nonnull final LibEGL libEGL,
                 @Nonnull final Pointer eglDisplay,
                 @Nonnull final Pointer eglSurface,
                 @Nonnull final Pointer eglContext) {
        this.libEGL = libEGL;
        this.eglDisplay = eglDisplay;
        this.eglSurface = eglSurface;
        this.eglContext = eglContext;
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
}
