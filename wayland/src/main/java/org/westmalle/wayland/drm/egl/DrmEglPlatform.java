//Copyright 2016 Erik De Rijcke
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
package org.westmalle.wayland.drm.egl;

import com.google.auto.factory.AutoFactory;
import org.westmalle.wayland.core.EglPlatform;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;


@AutoFactory(allowSubclasses = true,
             className = "PrivateDrmEglPlatformFactory")
public class DrmEglPlatform implements EglPlatform {


    private final long                            gbmDevice;
    private final long                            eglDisplay;
    private final long                            eglContext;
    private final String                          eglExtensions;
    @Nonnull
    private final List<Optional<DrmEglConnector>> gbmEglConnectors;

    DrmEglPlatform(final long gbmDevice,
                   final long eglDisplay,
                   final long eglContext,
                   final String eglExtensions,
                   @Nonnull final List<Optional<DrmEglConnector>> gbmEglConnectors) {
        this.gbmDevice = gbmDevice;
        this.eglDisplay = eglDisplay;
        this.eglContext = eglContext;
        this.eglExtensions = eglExtensions;
        this.gbmEglConnectors = gbmEglConnectors;
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
    public List<Optional<DrmEglConnector>> getConnectors() {
        return this.gbmEglConnectors;
    }

    @Nonnull
    @Override
    public String getEglExtensions() {
        return this.eglExtensions;
    }

    public long getGbmDevice() {
        return this.gbmDevice;
    }
}
