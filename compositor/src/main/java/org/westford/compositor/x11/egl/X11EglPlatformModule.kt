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
package org.westford.compositor.x11.egl

import dagger.Module
import dagger.Provides
import org.westford.compositor.x11.X11PlatformModule

import javax.inject.Singleton

@Module(includes = arrayOf(X11PlatformModule::class))
class X11EglPlatformModule {
    @Provides
    @Singleton
    internal fun provideX11EglPlatform(x11EglPlatformFactory: X11EglPlatformFactory): X11EglPlatform {
        return x11EglPlatformFactory.create()
    }
}
