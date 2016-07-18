package org.westmalle.wayland.bootstrap.html5;

import dagger.Module;
import dagger.Provides;
import org.westmalle.wayland.core.Platform;
import org.westmalle.wayland.html5.egl.Html5EglPlatformFactory;
import org.westmalle.wayland.x11.egl.X11EglPlatform;

import javax.inject.Singleton;

@Module
public class Html5X11EglPlatformAdaptorModule {
    @Provides
    @Singleton
    Platform providePlatform(final Html5EglPlatformFactory x11EglPlatformFactory,
                             final X11EglPlatform x11EglPlatform) {
        return x11EglPlatformFactory.create(x11EglPlatform);
    }
}
