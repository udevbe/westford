package org.westmalle.wayland.x11;

import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

@Module
public class X11PlatformModule {

    @Provides
    @Singleton
    X11Platform createX11Platform(final X11PlatformFactory x11PlatformFactory) {
        return x11PlatformFactory.create();
    }
}
