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
package org.westmalle.wayland.core;

import org.freedesktop.wayland.server.WlBufferResource;
import org.freedesktop.wayland.server.WlSurfaceResource;

import javax.annotation.Nonnull;

public interface Renderer {
    void visit(@Nonnull RenderOutput renderOutput);

    void visit(@Nonnull EglRenderOutput eglConnector);

    //TODO pixman sw rendering platform
    //void visit(PixmanPlatform pixmanPlatform);
    
    /**
     * @param wlSurfaceResource
     *
     * @deprecated method will be removed
     */
    //FIXME remove this method and instead register a destroy listener in the renderer implementation
    @Deprecated
    void onDestroy(@Nonnull WlSurfaceResource wlSurfaceResource);

    @Nonnull
    Buffer queryBuffer(@Nonnull WlBufferResource wlBufferResource);
}
