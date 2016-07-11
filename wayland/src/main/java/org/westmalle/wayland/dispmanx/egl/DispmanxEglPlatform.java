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
package org.westmalle.wayland.dispmanx.egl;

import com.google.auto.factory.AutoFactory;
import org.westmalle.wayland.core.EglPlatform;
import org.westmalle.wayland.core.Renderer;
import org.westmalle.wayland.dispmanx.DispmanxPlatform;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

@AutoFactory(className = "PrivateDispmanxEglPlatformFactory",
             allowSubclasses = true)
//TODO unit tests
public class DispmanxEglPlatform implements EglPlatform {

    @Nonnull
    private final DispmanxPlatform                     dispmanxPlatform;
    @Nonnull
    private final List<Optional<DispmanxEglConnector>> dispmanxEglConnectors;
    private final long                                 eglDisplay;
    private final long                                 eglContext;
    @Nonnull
    private final String                               eglExtensions;

    DispmanxEglPlatform(@Nonnull final DispmanxPlatform dispmanxPlatform,
                        @Nonnull final List<Optional<DispmanxEglConnector>> dispmanxEglConnectors,
                        final long eglDisplay,
                        final long eglContext,
                        @Nonnull final String eglExtensions) {
        this.dispmanxPlatform = dispmanxPlatform;
        this.dispmanxEglConnectors = dispmanxEglConnectors;
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
    public List<Optional<DispmanxEglConnector>> getConnectors() {
        return this.dispmanxEglConnectors;
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
