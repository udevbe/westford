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
package org.westmalle.compositor.bootstrap.x11.html5;

import dagger.Component;
import org.westmalle.compositor.core.CoreModule;
import org.westmalle.compositor.core.LifeCycle;
import org.westmalle.compositor.gles2.Gles2RendererModule;
import org.westmalle.compositor.protocol.WlSeat;
import org.westmalle.compositor.x11.egl.X11EglPlatformModule;
import org.westmalle.launch.direct.DirectModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {DirectModule.class,
                      CoreModule.class,
                      Gles2RendererModule.class,
                      X11EglPlatformModule.class,
                      Html5X11EglPlatformAdaptorModule.class})
public interface Html5X11EglCompositor {

    LifeCycle lifeCycle();

    /*
     * X11 egl platform provides a single seat.
     */
    WlSeat wlSeat();
}
