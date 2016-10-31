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
package org.westmalle.compositor.html5.egl;

import com.google.auto.factory.AutoFactory;
import org.westmalle.Signal;
import org.westmalle.Slot;
import org.westmalle.compositor.core.EglPlatform;
import org.westmalle.compositor.core.events.RenderOutputDestroyed;
import org.westmalle.compositor.core.events.RenderOutputNew;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.List;

@AutoFactory(allowSubclasses = true,
             className = "PrivateHtml5EglPlatformFactory")
public class Html5EglPlatform implements EglPlatform {

    @Nonnull
    private final EglPlatform          eglPlatform;
    @Nonnull
    private final List<Html5EglOutput> html5EglRenderOutputs;
    private final Signal<RenderOutputNew, Slot<RenderOutputNew>>             renderOutputNewSignal       = new Signal<>();
    private final Signal<RenderOutputDestroyed, Slot<RenderOutputDestroyed>> renderOutputDestroyedSignal = new Signal<>();

    @Inject
    Html5EglPlatform(@Nonnull final EglPlatform eglPlatform,
                     @Nonnull final List<Html5EglOutput> html5EglRenderOutputs) {
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
    public List<Html5EglOutput> getRenderOutputs() {
        return this.html5EglRenderOutputs;
    }

    @Override
    public Signal<RenderOutputNew, Slot<RenderOutputNew>> getRenderOutputNewSignal() {
        return this.renderOutputNewSignal;
    }

    @Override
    public Signal<RenderOutputDestroyed, Slot<RenderOutputDestroyed>> getRenderOutputDestroyedSignal() {
        return this.renderOutputDestroyedSignal;
    }

    @Nonnull
    @Override
    public String getEglExtensions() {
        return this.eglPlatform.getEglExtensions();
    }
}
