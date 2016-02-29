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
import org.westmalle.wayland.core.RenderOutput;
import org.westmalle.wayland.nativ.libEGL.LibEGL;

import javax.annotation.Nonnull;

@AutoFactory(className = "PrivateDispmanxEglOutputFactory",
             allowSubclasses = true)
//TODO unit tests
public class DispmanxEglOutput implements RenderOutput {

    @Nonnull
    private final LibEGL libEGL;
    private final long   eglDisplay;
    private final long   eglSurface;
    private final long   eglContext;

    DispmanxEglOutput(@Provided @Nonnull final LibEGL libEGL,
                      final long eglDisplay,
                      final long eglSurface,
                      final long eglContext) {
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
