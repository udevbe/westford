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
package org.westmalle.wayland.html5.egl;

import com.google.auto.factory.AutoFactory;
import org.westmalle.wayland.core.EglRenderPlatform;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.List;

@AutoFactory(allowSubclasses = true,
             className = "PrivateHtml5EglPlatformFactory")
public class Html5EglRenderPlatform implements EglRenderPlatform {

    @Nonnull
    private final EglRenderPlatform          eglPlatform;
    @Nonnull
    private final List<Html5EglRenderOutput> html5EglRenderOutputs;

    @Inject
    Html5EglRenderPlatform(@Nonnull final EglRenderPlatform eglPlatform,
                           @Nonnull final List<Html5EglRenderOutput> html5EglRenderOutputs) {
        this.eglPlatform = eglPlatform;
        this.html5EglRenderOutputs = html5EglRenderOutputs;
    }

    @Override
    public long getEglDisplay() {
        return this.eglPlatform.getEglDisplay();
    }

    @Override
    public long getEglContext() {
        return this.eglPlatform.getEglContext();
    }

    @Nonnull
    @Override
    public List<Html5EglRenderOutput> getRenderOutputs() {
        return this.html5EglRenderOutputs;
    }

    @Nonnull
    @Override
    public String getEglExtensions() {
        return this.eglPlatform.getEglExtensions();
    }
}
