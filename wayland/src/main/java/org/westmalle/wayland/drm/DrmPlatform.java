/*
 * Westmalle Wayland Compositor.
 * Copyright (C) 2016  Erik De Rijcke
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
