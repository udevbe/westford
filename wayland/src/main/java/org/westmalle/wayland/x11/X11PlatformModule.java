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
package org.westmalle.wayland.x11;

import dagger.Module;
import dagger.Provides;
import org.westmalle.wayland.protocol.WlSeat;
import org.westmalle.wayland.x11.config.X11PlatformConfig;

import javax.inject.Singleton;

@Module
public class X11PlatformModule {

    private final X11PlatformConfig x11PlatformConfig;

    public X11PlatformModule(final X11PlatformConfig x11PlatformConfig) {
        this.x11PlatformConfig = x11PlatformConfig;
    }

    @Provides
    @Singleton
    X11Platform createX11Platform(final X11PlatformFactory x11PlatformFactory) {
        return x11PlatformFactory.create();
    }

    @Provides
    @Singleton
    WlSeat createWlSeat(final X11SeatFactory x11SeatFactory) {
        return x11SeatFactory.create();
    }

    @Provides
    @Singleton
    X11PlatformConfig provideX11PlatformConfig() {
        return this.x11PlatformConfig;
    }
}
