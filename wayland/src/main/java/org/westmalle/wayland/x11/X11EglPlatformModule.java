package org.westmalle.wayland.x11;

import dagger.Module;
import dagger.Provides;
import org.westmalle.wayland.core.Platform;
import org.westmalle.wayland.protocol.WlSeat;

import javax.inject.Singleton;

@Module
public class X11EglPlatformModule {

    @Provides
    @Singleton
    X11Platform createX11Platform(final X11PlatformFactory x11PlatformFactory) {
        //FIXME from config
        return x11PlatformFactory.create(":0",
                                         800,
                                         600);
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
