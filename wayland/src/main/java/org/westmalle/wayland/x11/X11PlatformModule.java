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
