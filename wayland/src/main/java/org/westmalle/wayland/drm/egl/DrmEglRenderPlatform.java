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
package org.westmalle.wayland.drm.egl;

import com.google.auto.factory.AutoFactory;
import org.westmalle.wayland.core.EglRenderPlatform;

import javax.annotation.Nonnull;
import java.util.List;


@AutoFactory(allowSubclasses = true,
             className = "PrivateDrmEglPlatformFactory")
public class DrmEglRenderPlatform implements EglRenderPlatform {


    private final long gbmDevice;

    private final long   eglDisplay;
    private final long   eglContext;
    private final String eglExtensions;

    @Nonnull
    private final List<DrmEglRenderOutput> drmEglRenderOutputs;

    DrmEglRenderPlatform(final long gbmDevice,
                         final long eglDisplay,
                         final long eglContext,
                         final String eglExtensions,
                         @Nonnull final List<DrmEglRenderOutput> drmEglRenderOutputs) {
        this.gbmDevice = gbmDevice;
        this.eglDisplay = eglDisplay;
        this.eglContext = eglContext;
        this.eglExtensions = eglExtensions;
        this.drmEglRenderOutputs = drmEglRenderOutputs;
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
    public List<DrmEglRenderOutput> getRenderOutputs() {
        return this.drmEglRenderOutputs;
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
