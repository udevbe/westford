package org.westmalle.wayland.bootstrap.x11;

import dagger.Module;
import dagger.Provides;
import org.westmalle.wayland.core.Platform;
import org.westmalle.wayland.x11.egl.X11EglPlatform;

import javax.inject.Singleton;

@Module
public class X11EglPlatformAdaptorModule {

    @Provides
    @Singleton
    Platform providePlatform(final X11EglPlatform x11EglPlatform) {
        return x11EglPlatform;
    }
}
