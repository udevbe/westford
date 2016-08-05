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
import com.google.auto.factory.Provided;
import org.westmalle.wayland.core.EglPlatform;
import org.westmalle.wayland.core.Renderer;
import org.westmalle.wayland.html5.Html5Platform;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

@AutoFactory(allowSubclasses = true,
             className = "PrivateHtml5EglPlatformFactory")
public class Html5EglPlatform implements EglPlatform {

    @Nonnull
    private final EglPlatform                       eglPlatform;
    @Nonnull
    private final List<Optional<Html5EglConnector>> eglConnectors;

    @Inject
    Html5EglPlatform(@Nonnull final EglPlatform eglPlatform,
                     @Nonnull final List<Optional<Html5EglConnector>> eglConnectors) {
        this.eglPlatform = eglPlatform;
        this.eglConnectors = eglConnectors;
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
    public List<Optional<Html5EglConnector>> getConnectors() {
        return this.eglConnectors;
    }

    @Nonnull
    @Override
    public String getEglExtensions() {
        return this.eglPlatform.getEglExtensions();
    }
}
