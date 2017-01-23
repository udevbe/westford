/*
 * Westford Wayland Compositor.
 * Copyright (C) 2017  Erik De Rijcke
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


import org.westford.compositor.drm.egl.DrmEglOutput;
import org.westford.compositor.x11.egl.X11EglOutput;

public interface FullscreenRenderer extends Renderer {
    void visit(DrmEglOutput drmEglOutput);

    void visit(X11EglOutput x11EglOutput);

    //TODO pixman sw rendering platform
    //void visit(DrmPixmanOutput drmPixmanOutput);

    //TODO pixman sw rendering platform
    //void visit(X11PixmanOutput x11PixmanOutput);
}
