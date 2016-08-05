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
package org.westmalle.wayland.dispmanx.egl;

import dagger.Module;
import dagger.Provides;
import org.westmalle.wayland.core.Platform;
import org.westmalle.wayland.dispmanx.DispmanxPlatform;
import org.westmalle.wayland.dispmanx.DispmanxPlatformFactory;

import javax.annotation.Nonnull;
import javax.inject.Singleton;

import static org.westmalle.wayland.nativ.libbcm_host.Libbcm_host.DISPMANX_ID_HDMI;

@Module
public class DispmanxEglPlatformModule {

    @Provides
    @Singleton
    DispmanxPlatform createDispmanxPlatform(final DispmanxPlatformFactory dispmanxPlatformFactory) {
        //FIXME from config
        return dispmanxPlatformFactory.create(DISPMANX_ID_HDMI);
    }

    @Provides
    @Singleton
    Platform createPlatform(@Nonnull final DispmanxEglPlatformFactory dispmanxEglPlatformFactory) {
        return dispmanxEglPlatformFactory.create();
    }
}
