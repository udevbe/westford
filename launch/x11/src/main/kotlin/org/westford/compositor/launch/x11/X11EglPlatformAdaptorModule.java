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
package org.westford.compositor.launch.x11;

import dagger.Module;
import dagger.Provides;
import org.westford.compositor.core.RenderPlatform;
import org.westford.compositor.x11.egl.X11EglPlatform;

import javax.inject.Singleton;

@Module
public class X11EglPlatformAdaptorModule {

    @Provides
    @Singleton
    RenderPlatform providePlatform(final X11EglPlatform x11EglPlatform) {
        return x11EglPlatform;
    }
}
