package org.westmalle.wayland.html5.egl;


import dagger.Module;
import dagger.Provides;
import org.westmalle.wayland.core.Platform;

import javax.inject.Singleton;

@Module
public class Html5EglPlatformModule {

    @Provides
    @Singleton
    Platform providePlatform(final Html5EglPlatformFactory html5EglPlatformFactory) {
        return html5EglPlatformFactory.create();
    }
}
