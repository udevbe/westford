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
package org.westford.compositor.launch.drm.direct;

import dagger.Component;
import org.freedesktop.wayland.server.Display;
import org.westford.compositor.core.CoreModule;
import org.westford.compositor.core.KeyBindingFactory;
import org.westford.compositor.core.LifeCycle;
import org.westford.compositor.drm.egl.DrmEglPlatformModule;
import org.westford.compositor.gles2.Gles2RendererModule;
import org.westford.compositor.input.LibinputSeatFactory;
import org.westford.launch.LifeCycleSignals;
import org.westford.launch.direct.DirectModule;
import org.westford.nativ.glibc.Libc;
import org.westford.tty.Tty;
import org.westford.tty.TtyModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {CoreModule.class,
                      Gles2RendererModule.class,
                      DrmEglPlatformModule.class,
                      TtyModule.class,
                      DirectModule.class})
public interface DirectDrmEglCompositor {

    Display display();

    LifeCycle lifeCycle();

    LifeCycleSignals lifeCycleSignals();

    Libc libc();

    LibinputSeatFactory seatFactory();

    KeyBindingFactory keyBindingFactory();

    Tty tty();
}
