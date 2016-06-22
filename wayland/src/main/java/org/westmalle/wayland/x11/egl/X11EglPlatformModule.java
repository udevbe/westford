package org.westmalle.wayland.x11.egl;

import dagger.Module;
import dagger.Provides;
import org.westmalle.wayland.core.Platform;
import org.westmalle.wayland.protocol.WlSeat;
import org.westmalle.wayland.x11.X11Platform;
import org.westmalle.wayland.x11.X11PlatformFactory;
import org.westmalle.wayland.x11.X11SeatFactory;

import javax.inject.Singleton;

@Module
public class X11EglPlatformModule {

    @Provides
    @Singleton
    X11Platform createX11Platform(final X11PlatformFactory x11PlatformFactory) {
        return x11PlatformFactory.createX11Connector();
    }

    @Provides
    @Singleton
    Platform createPlatform(final X11EglPlatformFactory x11EglPlatformFactory) {
        return x11EglPlatformFactory.create();
    }

    @Provides
    @Singleton
    WlSeat createWlSeat(final X11SeatFactory x11SeatFactory) {
        return x11SeatFactory.create();
    }
}
