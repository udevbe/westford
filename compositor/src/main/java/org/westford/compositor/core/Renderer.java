/*
 * Westford Wayland Compositor.
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
package org.westford.compositor.core;

import org.freedesktop.wayland.server.WlBufferResource;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.westford.compositor.drm.egl.DrmEglOutput;
import org.westford.compositor.protocol.WlOutput;
import org.westford.compositor.x11.egl.X11EglOutput;

import javax.annotation.Nonnull;

public interface Renderer {
    default void visit(@Nonnull RenderOutput renderOutput,
                       @Nonnull WlOutput wlOutput) {
        throw new UnsupportedOperationException(String.format("RenderOutput %s not supported.",
                                                              renderOutput));
    }

    default void visit(@Nonnull EglOutput eglOutput,
                       @Nonnull WlOutput wlOutput) {
        throw new UnsupportedOperationException(String.format("EglOutput %s not supported.",
                                                              eglOutput));
    }

    default void visit(@Nonnull DrmEglOutput drmEglOutput,
                       @Nonnull WlOutput wlOutput) {
        throw new UnsupportedOperationException(String.format("DrmEglOutput %s not supported.",
                                                              drmEglOutput));
    }

    default void visit(@Nonnull X11EglOutput x11EglOutput,
                       @Nonnull WlOutput wlOutput) {
        throw new UnsupportedOperationException(String.format("X11EglOutput %s not supported.",
                                                              x11EglOutput));
    }

    //TODO pixman sw rendering platform
    //void visit(PixmanOutput pixmanOutput);
    //void visit(DrmPixmanOutput drmPixmanOutput);
    //void visit(X11PixmanOutput x11PixmanOutput);

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
