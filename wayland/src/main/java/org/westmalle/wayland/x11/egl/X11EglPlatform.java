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
package org.westmalle.wayland.x11.egl;

import com.google.auto.factory.AutoFactory;
import org.westmalle.wayland.core.EglPlatform;
import org.westmalle.wayland.core.Renderer;
import org.westmalle.wayland.x11.X11Platform;

import javax.annotation.Nonnull;

@AutoFactory(className = "PrivateX11EglPlatformFactory",
             allowSubclasses = true)
public class X11EglPlatform implements EglPlatform {

    @Nonnull
    private final X11Platform       x11Platform;
    @Nonnull
    private final X11EglConnector[] eglConnectors;

    private final long eglDisplay;
    private final long eglContext;

    @Nonnull
    private final String eglExtensions;

    X11EglPlatform(@Nonnull final X11Platform x11Platform,
                   @Nonnull final X11EglConnector[] eglConnectors,
                   final long eglDisplay,
                   final long eglContext,
                   @Nonnull final String eglExtensions) {
        this.x11Platform = x11Platform;
        this.eglConnectors = eglConnectors;
        this.eglDisplay = eglDisplay;
        this.eglContext = eglContext;
        this.eglExtensions = eglExtensions;
    }

    @Override
    public long getEglDisplay() {
        return this.eglDisplay;
    }

    @Override
    public long getEglContext() {
        return this.eglContext;
    }

    @Nonnull
    @Override
    public X11EglConnector[] getConnectors() {
        return this.eglConnectors;
    }

    @Override
    public void accept(@Nonnull final Renderer renderer) {
        renderer.visit(this);
    }

    @Nonnull
    public X11Platform getX11Platform() {
        return this.x11Platform;
    }

    @Nonnull
    @Override
    public String getEglExtensions() {
        return this.eglExtensions;
    }
}
