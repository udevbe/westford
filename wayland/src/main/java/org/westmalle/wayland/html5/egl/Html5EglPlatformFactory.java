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


import org.westmalle.wayland.core.EglRenderOutput;
import org.westmalle.wayland.core.EglRenderPlatform;
import org.westmalle.wayland.html5.Html5RenderOutput;
import org.westmalle.wayland.html5.Html5RenderPlatform;
import org.westmalle.wayland.html5.Html5PlatformFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Html5EglPlatformFactory {

    @Nonnull
    private final Html5PlatformFactory           html5PlatformFactory;
    @Nonnull
    private final PrivateHtml5EglPlatformFactory privateHtml5EglPlatformFactory;
    @Nonnull
    private final Html5EglRenderOutputFactory    html5EglRenderOutputFactory;

    @Inject
    Html5EglPlatformFactory(@Nonnull final Html5PlatformFactory html5PlatformFactory,
                            @Nonnull final PrivateHtml5EglPlatformFactory privateHtml5EglPlatformFactory,
                            @Nonnull final Html5EglRenderOutputFactory html5EglRenderOutputFactory) {
        this.html5PlatformFactory = html5PlatformFactory;
        this.privateHtml5EglPlatformFactory = privateHtml5EglPlatformFactory;
        this.html5EglRenderOutputFactory = html5EglRenderOutputFactory;
    }

    public Html5EglRenderPlatform create(@Nonnull final EglRenderPlatform eglPlatform) {

        final Html5RenderPlatform html5Platform = this.html5PlatformFactory.create(eglPlatform);

        final List<? extends EglRenderOutput> eglRenderOutputs   = eglPlatform.getRenderOutputs();
        final List<Html5RenderOutput>         html5RenderOutputs = html5Platform.getRenderOutputs();

        final Iterator<? extends EglRenderOutput> eglRenderOutputIterator   = eglRenderOutputs.iterator();
        final Iterator<Html5RenderOutput>         html5RenderOutputIterator = html5RenderOutputs.iterator();

        final List<Html5EglRenderOutput> html5EglRenderOutputs = new LinkedList<>();

        while (eglRenderOutputIterator.hasNext() &&
               html5RenderOutputIterator.hasNext()) {

            final EglRenderOutput   eglRenderOutput   = eglRenderOutputIterator.next();
            final Html5RenderOutput html5RenderOutput = html5RenderOutputIterator.next();

            html5EglRenderOutputs.add(this.html5EglRenderOutputFactory.create(html5RenderOutput,
                                                                              eglRenderOutput));
        }

        return this.privateHtml5EglPlatformFactory.create(eglPlatform,
                                                          html5EglRenderOutputs);
    }
}
