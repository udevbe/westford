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
