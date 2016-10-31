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
package org.westford.compositor.drm.egl;

import dagger.Module;
import dagger.Provides;
import org.westford.compositor.core.RenderPlatform;
import org.westford.compositor.drm.DrmPlatform;
import org.westford.compositor.drm.DrmPlatformFactory;
import org.westford.tty.TtyModule;

import javax.inject.Singleton;

@Module(includes = {TtyModule.class})
public class DrmEglPlatformModule {

    @Provides
    @Singleton
    DrmPlatform createDrmPlatform(final DrmPlatformFactory drmPlatformFactory) {
        return drmPlatformFactory.create();
    }

    @Provides
    @Singleton
    RenderPlatform createPlatform(final DrmEglPlatformFactory drmEglPlatformFactory) {
        return drmEglPlatformFactory.create();
    }
}
