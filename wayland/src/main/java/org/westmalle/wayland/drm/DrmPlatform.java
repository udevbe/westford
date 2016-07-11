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
package org.westmalle.wayland.drm;

import com.google.auto.factory.AutoFactory;
import org.westmalle.wayland.core.Platform;
import org.westmalle.wayland.core.Renderer;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

//TODO drm platform, remove all gbm dependencies
@AutoFactory(allowSubclasses = true,
             className = "PrivateDrmPlatformFactory")
public class DrmPlatform implements Platform {

    private final long                         drmDevice;
    private final int                          drmFd;
    @Nonnull
    private final DrmEventBus                  drmEventBus;
    @Nonnull
    private final List<Optional<DrmConnector>> drmConnectors;

    DrmPlatform(final long drmDevice,
                final int drmFd,
                @Nonnull final DrmEventBus drmEventBus,
                @Nonnull final List<Optional<DrmConnector>> drmConnectors) {
        this.drmDevice = drmDevice;
        this.drmFd = drmFd;
        this.drmEventBus = drmEventBus;
        this.drmConnectors = drmConnectors;
    }

    @Nonnull
    @Override
    public List<Optional<DrmConnector>> getConnectors() {
        return this.drmConnectors;
    }

    @Nonnull
    public DrmEventBus getDrmEventBus() {
        return this.drmEventBus;
    }

    public long getDrmDevice() {
        return this.drmDevice;
    }

    public int getDrmFd() {
        return this.drmFd;
    }
}
