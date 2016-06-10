package org.westmalle.wayland.x11;

import dagger.Module;
import dagger.Provides;
import org.westmalle.wayland.core.Platform;

import javax.inject.Singleton;

@Module
public class X11EglPlatformModule {

    @Provides
    @Singleton
    X11Platform createX11Platform(X11PlatformFactory x11PlatformFactory) {
        //FIXME from config
        return x11PlatformFactory.create(":0",
                                         800,
                                         600);
    }

    @Provides
    @Singleton
    Platform createPlatform(X11EglPlatformFactory x11EglPlatformFactory) {
        return x11EglPlatformFactory.create();
    }
}
